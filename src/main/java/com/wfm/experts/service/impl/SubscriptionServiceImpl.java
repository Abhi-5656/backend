package com.wfm.experts.service.impl;

import com.wfm.experts.tenant.common.core.Subscription;
import com.wfm.experts.tenant.common.employees.entity.Employee;
import com.wfm.experts.tenant.common.employees.entity.PersonalInfo;
import com.wfm.experts.tenant.common.employees.entity.OrganizationalInfo;
import com.wfm.experts.tenant.common.employees.entity.EmploymentDetails;
import com.wfm.experts.tenant.common.employees.entity.JobContextDetails;
import com.wfm.experts.tenant.common.employees.entity.Role;
import com.wfm.experts.tenant.common.employees.enums.EmploymentStatus;
import com.wfm.experts.tenant.common.employees.enums.EmploymentType;
import com.wfm.experts.tenant.common.employees.enums.WorkMode;
// Only import enums for PersonalInfo if you decide to set default values for admin for fields not provided
// import com.wfm.experts.entity.tenant.common.enums.Gender;
// import com.wfm.experts.entity.tenant.common.enums.MaritalStatus;
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
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
// import java.util.Optional; // Not directly used here but useful generally
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

    @Transactional
    @Override
    public Subscription createSubscription(
            Subscription subscription,
            String firstName,
            String lastName,
            String email,
            String employeeId,
            String phoneNumber) throws Exception {

        // ... (GST validation, tenantId generation, schema creation as before) ...
        if (!isValidGstNumber(subscription.getCompanyGstNumber())) {
            throw new IllegalArgumentException("Invalid GST Number format.");
        }

        String tenantId = TenantIdUtil.generateTenantId(subscription.getCompanyName());
        if (subscriptionRepository.existsByTenantId(tenantId)) {
            throw new IllegalStateException("Tenant ID already exists: " + tenantId + ". Company name might be too similar to an existing one.");
        }

        Map<String, Object> tenantData = tenantService.createTenantSchema(subscription.getCompanyName());
        if (tenantData == null || !tenantData.containsKey("tenantSchema")) {
            throw new RuntimeException("Error: Tenant schema creation failed.");
        }
        String tenantSchemaName = (String) tenantData.get("tenantSchema");

        String tenantURL = generateTenantURL(tenantId);

        subscription.setTenantId(tenantId);
        subscription.setTenantSchema(tenantSchemaName);
        subscription.setAdminEmail(email);
        subscription.setStatus("ACTIVE");
        if (subscription.getTransactionId() == null) {
            subscription.setTransactionId("TXN-" + System.currentTimeMillis());
        }
        if (subscription.getCurrency() == null) {
            subscription.setCurrency("INR");
        }
        subscription.setPurchaseDate(new Date());
        subscription.setActivationDate(new Date());
        subscription.setTenantURL(tenantURL);

        Subscription savedSubscription = subscriptionRepository.save(subscription);
        LOGGER.log(Level.INFO, "Subscription saved for tenant ID: {0}", tenantId);


        createAdminEmployeeInTenant(tenantId, firstName, lastName, email, employeeId, phoneNumber);

        return savedSubscription;
    }

    private void createAdminEmployeeInTenant(String tenantId, String firstName, String lastName, String email,
                                             String employeeId, String phoneNumber) {
        Role adminRole = roleRepository.findByRoleName("ADMIN")
                .orElseGet(() -> {
                    LOGGER.log(Level.WARNING, "ADMIN role not found in schema for tenant {0}, creating it.", tenantId);
                    Role newAdminRole = new Role();
                    newAdminRole.setRoleName("ADMIN");
                    return roleRepository.save(newAdminRole);
                });

        TenantContext.setTenant(tenantId);
        tenantSchemaUtil.ensureTenantSchemaIsSet();

        Employee adminEmployee = new Employee();

        adminEmployee.setEmployeeId(employeeId);
        adminEmployee.setEmail(email);

        // Generate raw password
        String rawPassword = generateRandomPassword();
        adminEmployee.setPassword(passwordEncoder.encode(rawPassword)); // Encode and set

        adminEmployee.setPhoneNumber(phoneNumber);
//        adminEmployee.setRole(adminRole);
        adminEmployee.setRoles(List.of(adminRole));

        adminEmployee.setTenantId(tenantId); // Set tenantId on employee object as well

        PersonalInfo personalInfo = new PersonalInfo();
        personalInfo.setFirstName(firstName);
        personalInfo.setLastName(lastName);
        adminEmployee.setPersonalInfo(personalInfo);

        OrganizationalInfo organizationalInfo = new OrganizationalInfo();
        EmploymentDetails employmentDetails = new EmploymentDetails();
        employmentDetails.setDateOfJoining(LocalDate.now());
        employmentDetails.setEmploymentType(EmploymentType.PERMANENT);
        employmentDetails.setEmploymentStatus(EmploymentStatus.ACTIVE);
        employmentDetails.setNoticePeriodDays(30);
        employmentDetails.setWorkMode(WorkMode.WORK_FROM_OFFICE);
        organizationalInfo.setEmploymentDetails(employmentDetails);

        JobContextDetails jobContextDetails = new JobContextDetails();
        jobContextDetails.setDepartmentName("Administration");
        jobContextDetails.setJobGradeBand("Senior Management");
        jobContextDetails.setCostCenter("Corporate");
        jobContextDetails.setOrganizationalRoleDescription("Tenant Administrator");
        organizationalInfo.setJobContextDetails(jobContextDetails);

        organizationalInfo.setOrgAssignmentEffectiveDate(LocalDate.now());
        adminEmployee.setOrganizationalInfo(organizationalInfo);

        try {
            Employee savedAdmin = employeeRepository.save(adminEmployee);
            LOGGER.log(Level.INFO, "Admin Employee Created for tenant {0}: {1} (ID: {2}).",
                    new Object[]{tenantId, savedAdmin.getEmail(), savedAdmin.getEmployeeId()});

            // Log the username, raw password (SECURITY RISK!), and tenantId
            // THIS IS A SECURITY RISK - DO NOT USE IN PRODUCTION
            System.out.println("--------------------------------------------------------------------------");
            System.out.println("ADMIN USER CREATED (FOR DEVELOPMENT/DEBUGGING ONLY - REMOVE FOR PRODUCTION):");
            System.out.println("Tenant ID: " + tenantId);
            System.out.println("Username (Email): " + email);
            System.out.println("Generated Raw Password: " + rawPassword); // SECURITY RISK
            System.out.println("Employee ID: " + employeeId);
            System.out.println("--------------------------------------------------------------------------");
            // END SECURITY RISK LOGGING

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving admin employee for tenant " + tenantId, e);
            throw new RuntimeException("Failed to create admin employee for tenant " + tenantId, e);
        } finally {
            TenantContext.clear();
        }
    }

    private String generateRandomPassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&!";
        int passwordLength = 12;
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(passwordLength);
        for (int i = 0; i < passwordLength; i++) {
            password.append(characters.charAt(random.nextInt(characters.length())));
        }
        // Do NOT log the rawPassword here if you intend to capture it later.
        // The previous System.out.println for "Generated Raw Password (for dev only):"
        // in this method is redundant if you log it after creation.
        return password.toString();
    }

    private String generateTenantURL(String tenantId) {
        String baseUrl;
        try {
            String hostAddress = environment.getProperty("server.address", "localhost");
            if ("0.0.0.0".equals(hostAddress)) {
                hostAddress = InetAddress.getLocalHost().getHostAddress();
            }
            String port = environment.getProperty("server.port", "8080");
            baseUrl = "http://" + hostAddress + ":" + port;
        } catch (UnknownHostException e) {
            LOGGER.log(Level.WARNING, "Could not determine host address, defaulting to localhost for tenant URL.", e);
            baseUrl = "http://localhost:8080";
        }
        return baseUrl + "/" + tenantId;
    }

    private boolean isValidGstNumber(String gstNumber) {
        if (gstNumber == null) return false;
        String gstRegex = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[A-Z0-9]{1}Z[A-Z0-9]{1}$";
        return gstNumber.matches(gstRegex);
    }
}