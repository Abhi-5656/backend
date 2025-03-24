package com.wfm.experts.service.impl;

import com.wfm.experts.entity.core.Subscription;
import com.wfm.experts.entity.tenant.common.Employee;
import com.wfm.experts.entity.tenant.common.Role;
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
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

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
            Environment environment) { // Inject Environment to get server port
        this.subscriptionRepository = subscriptionRepository;
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.tenantService = tenantService;
        this.passwordEncoder = passwordEncoder;
        this.tenantSchemaUtil = tenantSchemaUtil;
        this.environment = environment;
    }

    /**
     * ✅ Creates a new Tenant Subscription along with an Admin Employee.
     */
    @Transactional
    @Override
    public Subscription createSubscription(
            Subscription subscription,
            String firstName,
            String lastName,
            String email,
            String employeeId,
            String phoneNumber) throws Exception {

        // ✅ Validate GST Number Format
        if (!isValidGstNumber(subscription.getCompanyGstNumber())) {
            throw new IllegalArgumentException("Invalid GST Number format.");
        }

        // ✅ Generate `tenantId` from company name
        String tenantId = TenantIdUtil.generateTenantId(subscription.getCompanyName());

        // ✅ Step 1: Create Tenant Schema
        Map<String, Object> tenantData = tenantService.createTenantSchema(subscription.getCompanyName());
        if (tenantData == null || !tenantData.containsKey("tenantSchema")) {
            throw new RuntimeException("Error: Tenant schema creation failed.");
        }
        String tenantSchemaName = (String) tenantData.get("tenantSchema");

        // ✅ Step 2: Generate the Tenant URL
        String tenantURL = generateTenantURL(tenantId);

        // ✅ Step 3: Set Subscription Details
        subscription.setTenantId(tenantId);
        subscription.setTenantSchema(tenantSchemaName);
        subscription.setAdminEmail(email);
        subscription.setStatus("ACTIVE");
        subscription.setTransactionId("TXN-" + System.currentTimeMillis());
        subscription.setCurrency("INR");
        subscription.setPurchaseDate(new Date());
        subscription.setActivationDate(new Date());
        subscription.setTenantURL(tenantURL); // 🔹 Setting Tenant URL

        // ✅ Step 4: Save Subscription
        Subscription savedSubscription = subscriptionRepository.save(subscription);

        // ✅ Step 5: Create Admin Employee in Tenant Schema
        createEmployeeInTenant(tenantId, firstName, lastName, email, employeeId, phoneNumber);

        return savedSubscription;
    }

    /**
     * ✅ Creates an Admin Employee in the Tenant Schema.
     */
    private void createEmployeeInTenant(String tenantId, String firstName, String lastName, String email, String employeeId, String phoneNumber) {
        // ✅ Ensure ADMIN role exists
        Role adminRole = roleRepository.findByRoleName("ADMIN").orElseGet(() -> {
            Role newAdminRole = new Role();
            newAdminRole.setRoleName("ADMIN");
            return roleRepository.save(newAdminRole);
        });

        // ✅ Switch to tenant schema before creating the employee
        TenantContext.setTenant(tenantId);
        // ✅ Ensure Schema is Set Before Saving Employee (Fix for transactions)
        tenantSchemaUtil.ensureTenantSchemaIsSet();

        Employee employee = new Employee();
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setFullName(firstName.trim() + " " + lastName.trim());
        employee.setEmail(email);
        employee.setPhoneNumber(phoneNumber);
        employee.setRole(adminRole);
        employee.setEmployeeId(employeeId);
        employee.setTenantId(tenantId);

        String rawPassword = generateRandomPassword();
        employee.setPassword(passwordEncoder.encode(rawPassword));

        employeeRepository.save(employee);
        System.out.println("Admin Employee Created: " + email + " | Password: " + rawPassword);
    }

    /**
     * ✅ Generates a Secure Random Password.
     */
    private String generateRandomPassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&!";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            password.append(characters.charAt(random.nextInt(characters.length())));
        }
        return password.toString();
    }

    /**
     * ✅ Generates the Tenant URL dynamically based on the server IP and port.
     */
    private String generateTenantURL(String tenantId) {
        String baseUrl;
        try {
            // Fetch the IP address of the current machine (or default to localhost)
            String ip = InetAddress.getLocalHost().getHostAddress();
            String port = environment.getProperty("server.port", "8080"); // Default to 8080 if not found
            baseUrl = "http://" + ip + ":" + port;
        } catch (UnknownHostException e) {
            baseUrl = "http://localhost:8080"; // Fallback in case of error
        }

        return baseUrl + "/" + tenantId; // Format: http://<server-ip>:<port>/<tenantId>
    }

    /**
     * ✅ Validates GST Number Format.
     */
    private boolean isValidGstNumber(String gstNumber) {
        String gstRegex = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[0-9A-Z]{1}[Z]{1}[0-9A-Z]{1}$";
        return gstNumber != null && gstNumber.matches(gstRegex);
    }
}
