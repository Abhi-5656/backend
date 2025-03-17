package com.wfm.experts.service.impl;

import com.wfm.experts.entity.core.Subscription;
import com.wfm.experts.entity.tenant.common.Employee;
import com.wfm.experts.entity.tenant.common.Role;
import com.wfm.experts.repository.core.SubscriptionRepository;
import com.wfm.experts.repository.tenant.common.EmployeeRepository;
import com.wfm.experts.repository.tenant.common.RoleRepository;
import com.wfm.experts.service.SubscriptionService;
import com.wfm.experts.service.TenantService;
import jdk.swing.interop.SwingInterOpUtils;
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

    @Autowired
    public SubscriptionServiceImpl(
            SubscriptionRepository subscriptionRepository,
            EmployeeRepository employeeRepository,
            RoleRepository roleRepository,
            TenantService tenantService,
            PasswordEncoder passwordEncoder,
            EntityManager entityManager) {
        this.subscriptionRepository = subscriptionRepository;
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.tenantService = tenantService;
        this.passwordEncoder = passwordEncoder;
        this.entityManager = entityManager;
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

        // ✅ Validate GST Number Format
        if (!isValidGstNumber(subscription.getCompanyGstNumber())) {
            throw new IllegalArgumentException("❌ Invalid GST Number format.");
        }

        // ✅ Convert Company Name to Schema Format
        String companyName = subscription.getCompanyName();
        String tenantSchema = convertCompanyNameToSchema(companyName);

        // ✅ Step 1: Create Tenant Schema
        Map<String, Object> tenantData = tenantService.createTenantSchema(companyName);  // 🔥 Updated return type
        if (tenantData == null || !tenantData.containsKey("tenantId") || !tenantData.containsKey("tenantUrl")) {
            throw new RuntimeException("❌ Error: Tenant schema creation failed.");
        }

        // ✅ Use UUID directly (No Conversion Needed)
        UUID tenantId = (UUID) tenantData.get("tenantId");
        String tenantUrl = (String) tenantData.get("tenantUrl");

        System.out.println("🔹 Tenant Created: ID = " + tenantId + ", URL = " + tenantUrl);

        // ✅ Step 2: Assign Tenant ID & URL to Subscription **Before Saving**
        subscription.setTenantId(tenantId);  // ✅ Using UUID directly
        subscription.setTenantUrl(tenantUrl);
        subscription.setTenantSchema(tenantSchema);
        subscription.setAdminEmail(email);
        subscription.setStatus("ACTIVE");
        subscription.setTransactionId("TXN-" + System.currentTimeMillis());
        subscription.setPurchaseDate(new Date());
        subscription.setActivationDate(new Date());
        subscription.setCompanyGstNumber(subscription.getCompanyGstNumber());



        // ✅ Step 3: Save Subscription (JPA will auto-save modules)
        Subscription savedSubscription = subscriptionRepository.save(subscription);

        // ✅ Step 4: Switch to tenant schema and insert Employee
        switchToTenantSchema(tenantSchema);  // ✅ Switched to UUID-based tenant schema
        createEmployeeInTenant(tenantId, firstName, lastName, email, employeeId, phoneNumber);

        return savedSubscription;
    }


    @Override
    public Subscription getSubscriptionById(Long id) {
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Subscription not found!"));
    }

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
     * ✅ Creates an Employee in the Tenant with `tenantId`, ensuring `ADMIN` role exists.
     */
    private void createEmployeeInTenant(UUID tenantId, String firstName, String lastName, String email, String employeeId, String phoneNumber) {
        // ✅ Ensure ADMIN role exists (create it if necessary)
        Role adminRole = roleRepository.findByRoleName("ADMIN").orElseGet(() -> {
            Role newAdminRole = new Role();
            newAdminRole.setRoleName("ADMIN");
            return roleRepository.save(newAdminRole);
        });

        Employee employee = new Employee();
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setFullName(firstName.trim() + " " + lastName.trim());
        employee.setEmail(email);
        employee.setPhoneNumber(phoneNumber);
        employee.setRole(adminRole);  // ✅ Use Role entity
        employee.setEmployeeId(employeeId);
        employee.setTenantId(tenantId); // ✅ Associate Employee with Tenant

        String rawPassword = generateRandomPassword();
        employee.setPassword(passwordEncoder.encode(rawPassword));

        employeeRepository.save(employee);
        System.out.println("🔹 Admin Employee Created: " + email + " | Password: " + rawPassword);
    }

    /**
     * ✅ Switches to the Tenant Schema.
     */
    private void switchToTenantSchema(String tenantSchema) {
        entityManager.createNativeQuery("SET search_path TO " + tenantSchema).executeUpdate();
    }

    /**
     * ✅ Generates a Secure Random Password.
     */
    private String generateRandomPassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&!";
        Random random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 10; i++) {  // ✅ Generates a 10-character secure password
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
}
