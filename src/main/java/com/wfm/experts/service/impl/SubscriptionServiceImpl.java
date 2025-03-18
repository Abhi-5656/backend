package com.wfm.experts.service.impl;

import com.wfm.experts.entity.core.Subscription;
import com.wfm.experts.entity.tenant.common.Employee;
import com.wfm.experts.entity.tenant.common.Role;
import com.wfm.experts.repository.core.SubscriptionRepository;
import com.wfm.experts.repository.tenant.common.EmployeeRepository;
import com.wfm.experts.repository.tenant.common.RoleRepository;
import com.wfm.experts.service.SubscriptionService;
import com.wfm.experts.service.TenantService;
import com.wfm.experts.util.TenantSchemaUtil;
import com.wfm.experts.tenancy.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.*;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final TenantService tenantService;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;
    private final TenantSchemaUtil tenantSchemaUtil;

    @Autowired
    public SubscriptionServiceImpl(
            SubscriptionRepository subscriptionRepository,
            EmployeeRepository employeeRepository,
            RoleRepository roleRepository,
            TenantService tenantService,
            PasswordEncoder passwordEncoder,
            EntityManager entityManager,
            TenantSchemaUtil tenantSchemaUtil) {
        this.subscriptionRepository = subscriptionRepository;
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.tenantService = tenantService;
        this.passwordEncoder = passwordEncoder;
        this.entityManager = entityManager;
        this.tenantSchemaUtil = tenantSchemaUtil;
    }

    /**
     * ✅ Creates a new Tenant Subscription along with an Admin Employee.
     * ✅ Ensures schema creation happens **before** any transactions.
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

        // ✅ Convert Company Name to Schema Format
        String companyName = subscription.getCompanyName();
        String tenantSchema = convertCompanyNameToSchema(companyName);

        // ✅ Step 1: Create Tenant Schema **before saving anything**
        Map<String, Object> tenantData = tenantService.createTenantSchema(companyName);
        if (tenantData == null || !tenantData.containsKey("tenantId") || !tenantData.containsKey("tenantSchema")) {
            throw new RuntimeException("Error: Tenant schema creation failed.");
        }

        // ✅ Use UUID directly (No Conversion Needed)
        UUID tenantId = (UUID) tenantData.get("tenantId");
        String tenantSchemaName = (String) tenantData.get("tenantSchema");

        // ✅ Step 2: Assign Tenant ID & Schema to Subscription **Before Saving**
        subscription.setTenantId(tenantId);
        subscription.setTenantSchema(tenantSchemaName);
        subscription.setAdminEmail(email);
        subscription.setStatus("ACTIVE");
        subscription.setTransactionId("TXN-" + System.currentTimeMillis());
        // ✅ Set currency to INR by default
        subscription.setCurrency("INR");
        subscription.setPurchaseDate(new Date());
        subscription.setActivationDate(new Date());
        subscription.setCompanyGstNumber(subscription.getCompanyGstNumber());


        // ✅ Set company domain (the part after @ in the email)
        String companyDomain = extractCompanyDomain(email);  // Extract domain (after @) from the email
        subscription.setCompanyDomain(companyDomain);  // Set company domain in the subscription

        // ✅ Step 3: Save Subscription (No schema switch needed)
        Subscription savedSubscription = subscriptionRepository.save(subscription);

        // ✅ Step 4: Now switch schema and create employee in tenant schema
        createEmployeeInTenant(tenantId, firstName, lastName, email, employeeId, phoneNumber);

        return savedSubscription;
    }

    /**
     * ✅ Fetch Subscription by ID.
     */
    @Override
    public Subscription getSubscriptionById(Long id) {
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found!"));
    }

    /**
     * ✅ Fetch All Subscriptions.
     */
    @Override
    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }

    /**
     * ✅ Converts company name to a valid schema name.
     */
    private String convertCompanyNameToSchema(String companyName) {
        return Normalizer.normalize(companyName, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")  // Remove non-ASCII characters
                .replaceAll("\\s+", "_")         // Replace spaces with underscores
                .toLowerCase();
    }

    /**
     * ✅ Creates an Admin Employee in the Tenant Schema.
     */
    private void createEmployeeInTenant(UUID tenantId, String firstName, String lastName, String email, String employeeId, String phoneNumber) {
        // ✅ Ensure ADMIN role exists (create it if necessary)
        Role adminRole = roleRepository.findByRoleName("ADMIN").orElseGet(() -> {
            Role newAdminRole = new Role();
            newAdminRole.setRoleName("ADMIN");
            return roleRepository.save(newAdminRole);
        });

        // ✅ Switch to tenant schema before creating the employee
        TenantContext.setTenant(tenantId); // Set tenant context before schema switching
        tenantSchemaUtil.switchToTenantSchema(); // Switch schema to the correct tenant schema

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
        System.out.println("🔹 Admin Employee Created: " + email + " | Password: " + rawPassword);
    }

    /**
     * ✅ Generates a Secure Random Password.
     */
    private String generateRandomPassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&!";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 10; i++) {  // Generates a 10-character secure password
            password.append(characters.charAt(random.nextInt(characters.length())));
        }
        return password.toString();
    }

    /**
     * ✅ Validates GST Number Format.
     */
    private boolean isValidGstNumber(String gstNumber) {
        String gstRegex = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[0-9A-Z]{1}[Z]{1}[0-9A-Z]{1}$";
        return gstNumber != null && gstNumber.matches(gstRegex);
    }

    // Helper method to extract company domain (the part after @)
    private String extractCompanyDomain(String email) {
        // Extracting the domain part after '@'
        if (email != null && email.contains("@")) {
            return email.split("@")[1].toLowerCase();  // Return the part after @ (e.g., "wfmexperts.com")
        } else {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
    }
}
