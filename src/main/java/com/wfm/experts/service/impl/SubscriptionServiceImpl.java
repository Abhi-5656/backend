package com.wfm.experts.service.impl;

import com.wfm.experts.entity.core.Subscription;
import com.wfm.experts.entity.tenant.common.Employee;
import com.wfm.experts.entity.tenant.common.PersonalInfo; // Import PersonalInfo
import com.wfm.experts.entity.tenant.common.OrganizationalInfo; // Import OrganizationalInfo
import com.wfm.experts.entity.tenant.common.EmploymentDetails; // Import EmploymentDetails
import com.wfm.experts.entity.tenant.common.Role;
import com.wfm.experts.entity.tenant.common.enums.EmploymentStatus; // Import necessary enums
import com.wfm.experts.entity.tenant.common.enums.EmploymentTypeEnum;
import com.wfm.experts.repository.core.SubscriptionRepository;
import com.wfm.experts.repository.tenant.common.EmployeeRepository;
import com.wfm.experts.repository.tenant.common.RoleRepository;
import com.wfm.experts.service.SubscriptionService;
import com.wfm.experts.service.TenantService;
import com.wfm.experts.util.TenantIdUtil;
import com.wfm.experts.tenancy.TenantContext;
import com.wfm.experts.util.TenantSchemaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.time.LocalDate; // Import LocalDate
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final Logger LOGGER = Logger.getLogger(SubscriptionServiceImpl.class.getName());

    private final SubscriptionRepository subscriptionRepository;
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final TenantService tenantService;
    private final PasswordEncoder passwordEncoder;
    private final TenantSchemaUtil tenantSchemaUtil;
    private final Environment environment;

    @Autowired
    public SubscriptionServiceImpl(
            SubscriptionRepository subscriptionRepository,
            EmployeeRepository employeeRepository,
            RoleRepository roleRepository,
            TenantService tenantService,
            PasswordEncoder passwordEncoder,
            TenantSchemaUtil tenantSchemaUtil,
            Environment environment) {
        this.subscriptionRepository = subscriptionRepository;
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.tenantService = tenantService;
        this.passwordEncoder = passwordEncoder;
        this.tenantSchemaUtil = tenantSchemaUtil;
        this.environment = environment;
    }

    /**
     * Creates a new Tenant Subscription along with an Admin Employee.
     * The Admin Employee will have basic PersonalInfo and default OrganizationalInfo.
     */
    @Transactional
    @Override
    public Subscription createSubscription(
            Subscription subscription,
            String firstName,
            String lastName,
            String email,
            String employeeId, // This is the Employee Code for the admin
            String phoneNumber) throws Exception {

        // Validate GST Number Format
        if (!isValidGstNumber(subscription.getCompanyGstNumber())) {
            throw new IllegalArgumentException("Invalid GST Number format.");
        }

        // Generate `tenantId` from company name
        String tenantId = TenantIdUtil.generateTenantId(subscription.getCompanyName());
        if (subscriptionRepository.existsByTenantId(tenantId)) {
            throw new IllegalStateException("Tenant ID already exists: " + tenantId + ". Company name might be too similar to an existing one.");
        }


        // Step 1: Create Tenant Schema
        Map<String, Object> tenantData = tenantService.createTenantSchema(subscription.getCompanyName());
        if (tenantData == null || !tenantData.containsKey("tenantSchema")) {
            throw new RuntimeException("Error: Tenant schema creation failed.");
        }
        String tenantSchemaName = (String) tenantData.get("tenantSchema");

        // Step 2: Generate the Tenant URL
        String tenantURL = generateTenantURL(tenantId);

        // Step 3: Set Subscription Details
        subscription.setTenantId(tenantId);
        subscription.setTenantSchema(tenantSchemaName);
        subscription.setAdminEmail(email); // Storing the admin's email in the subscription record
        subscription.setStatus("ACTIVE");
        // Ensure transactionId is not null if the column doesn't have a default in DB
        if (subscription.getTransactionId() == null) {
            subscription.setTransactionId("TXN-" + System.currentTimeMillis());
        }
        // Ensure currency is not null
        if (subscription.getCurrency() == null) {
            subscription.setCurrency("INR");
        }
        subscription.setPurchaseDate(new Date());
        subscription.setActivationDate(new Date());
        subscription.setTenantURL(tenantURL);

        // Step 4: Save Subscription
        Subscription savedSubscription = subscriptionRepository.save(subscription);
        LOGGER.log(Level.INFO, "Subscription saved for tenant ID: {0}", tenantId);


        // Step 5: Create Admin Employee in Tenant Schema
        // Pass the necessary details to populate the new Employee structure
        createAdminEmployeeInTenant(tenantId, firstName, lastName, email, employeeId, phoneNumber);

        return savedSubscription;
    }

    /**
     * Creates an Admin Employee in the specified Tenant Schema with basic details.
     */
    private void createAdminEmployeeInTenant(String tenantId, String firstName, String lastName, String email,
                                             String employeeId, String phoneNumber) {
        // Ensure ADMIN role exists in the target tenant schema
        // Note: Role table is created by Flyway migration V1 for each tenant.
        Role adminRole = roleRepository.findByRoleName("ADMIN")
                .orElseGet(() -> {
                    LOGGER.log(Level.WARNING, "ADMIN role not found in schema for tenant {0}, creating it.", tenantId);
                    Role newAdminRole = new Role();
                    newAdminRole.setRoleName("ADMIN");
                    return roleRepository.save(newAdminRole); // This save will happen in the current tenant's schema
                });

        // Switch to tenant schema before creating the employee
        TenantContext.setTenant(tenantId);
        tenantSchemaUtil.ensureTenantSchemaIsSet(); // Ensure schema is switched for the current transaction/session

        Employee adminEmployee = new Employee();

        // --- Set Core Identifying and Auth Info ---
        adminEmployee.setEmployeeId(employeeId);
        adminEmployee.setEmail(email);
        adminEmployee.setPassword(passwordEncoder.encode(generateRandomPassword())); // Generate a random password
        adminEmployee.setPhoneNumber(phoneNumber);
        adminEmployee.setRole(adminRole);
        adminEmployee.setTenantId(tenantId); // Important for context if ever queried without tenant context

        // --- Set Personal Information ---
        PersonalInfo personalInfo = new PersonalInfo();
        personalInfo.setFirstName(firstName);
        personalInfo.setLastName(lastName);
        // fullName and displayName will be set by @PrePersist in Employee entity
        // Other PersonalInfo fields (gender, DOB, addresses, etc.) are left null for initial admin setup
        adminEmployee.setPersonalInfo(personalInfo);

        // --- Set Organizational Information (with defaults for EmploymentDetails) ---
        OrganizationalInfo organizationalInfo = new OrganizationalInfo();
        EmploymentDetails employmentDetails = new EmploymentDetails();

        employmentDetails.setDateOfJoining(LocalDate.now());
        employmentDetails.setEmploymentType(EmploymentTypeEnum.PERMANENT); // Default
        employmentDetails.setEmploymentStatus(EmploymentStatus.ACTIVE);    // Default
        employmentDetails.setRoleEffectiveDate(LocalDate.now());          // Default
        // Other EmploymentDetails (department, jobGrade, noticePeriod, etc.) are left null

        organizationalInfo.setEmploymentDetails(employmentDetails);
        adminEmployee.setOrganizationalInfo(organizationalInfo);

        // Direct organizational relationships (workLocation, businessUnit, jobTitle, managers)
        // are left null for the initial admin setup. The admin can configure these later.

        try {
            Employee savedAdmin = employeeRepository.save(adminEmployee);
            LOGGER.log(Level.INFO, "Admin Employee Created for tenant {0}: {1} (ID: {2}). Initial password needs to be communicated securely.",
                    new Object[]{tenantId, savedAdmin.getEmail(), savedAdmin.getEmployeeId()});
            // IMPORTANT: Communicate the generated password to the admin securely.
            // For a real application, you'd likely send a welcome email with a temporary password
            // or a password reset link. Logging it here is for development/debugging.
            System.out.println("Admin Employee Created for tenant " + tenantId + ": " + email + " | Employee ID: " + employeeId + " | Generated Password: [Password was generated and encoded]");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving admin employee for tenant " + tenantId, e);
            // This might indicate an issue with unique constraints or other DB problems.
            throw new RuntimeException("Failed to create admin employee for tenant " + tenantId, e);
        } finally {
            TenantContext.clear(); // Clear tenant context after operation
        }
    }

    /**
     * Generates a Secure Random Password.
     */
    private String generateRandomPassword() {
        // Consider making password policy configurable
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&!";
        int passwordLength = 12; // Increased length for better security
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(passwordLength);
        for (int i = 0; i < passwordLength; i++) {
            password.append(characters.charAt(random.nextInt(characters.length())));
        }
        // For actual use, ensure this generated password is communicated securely or is temporary
        // and forces a change on first login.
        System.out.println("Generated Raw Password (for dev only): " + password.toString());
        return password.toString();
    }

    /**
     * Generates the Tenant URL dynamically based on the server IP and port.
     */
    private String generateTenantURL(String tenantId) {
        String baseUrl;
        try {
            String hostAddress = environment.getProperty("server.address", "localhost");
            if ("0.0.0.0".equals(hostAddress)) { // if bound to all interfaces
                hostAddress = InetAddress.getLocalHost().getHostAddress();
            }
            String port = environment.getProperty("server.port", "8080");
            baseUrl = "http://" + hostAddress + ":" + port;
        } catch (UnknownHostException e) {
            LOGGER.log(Level.WARNING, "Could not determine host address, defaulting to localhost for tenant URL.", e);
            baseUrl = "http://localhost:8080"; // Fallback
        }
        return baseUrl + "/" + tenantId; // Example format: http://<server-ip>:<port>/<tenantId>
    }

    /**
     * Validates GST Number Format.
     * (Basic regex, can be enhanced for more specific Indian GST rules if needed)
     */
    private boolean isValidGstNumber(String gstNumber) {
        if (gstNumber == null) return false;
        // Regex for Indian GST Number: 2 digits (state code), 5 letters (PAN),
        // 4 digits (entity number), 1 letter (PAN check digit), 1 letter (Z default), 1 alphanumeric (checksum)
        String gstRegex = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[A-Z0-9]{1}Z[A-Z0-9]{1}$";
        return gstNumber.matches(gstRegex);
    }
}
