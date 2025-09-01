//package com.wfm.experts.service.impl;
//
//// Import necessary entities if you need to pre-populate them for bulk creation
//import com.wfm.experts.exception.InvalidEmailException;
//import com.wfm.experts.tenant.common.employees.repository.EmployeeRepository;
//import com.wfm.experts.service.EmployeeService;
//import com.wfm.experts.tenancy.TenantContext;
//import com.wfm.experts.tenant.common.employees.entity.Employee;
//import com.wfm.experts.setup.roles.entity.Role;
//import com.wfm.experts.util.TenantSchemaUtil;
//// Import validation groups if you intend to validate with a specific group
//// import com.wfm.experts.validation.groups.OnEmployeeProfile;
//// import jakarta.validation.Valid; // For method parameter validation
//// import org.springframework.validation.annotation.Validated; // For class/method level group validation
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//// import org.springframework.security.core.userdetails.UsernameNotFoundException; // Already handled by InvalidEmailException
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import jakarta.validation.Valid; // Import for @Valid
//
//        import java.util.ArrayList;
//        import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
///**
// *Implements `UserDetailsService` for Spring Security & CRUD operations for Employees.
// */
//@Service
//// If you want to apply validation groups to methods in this service:
//// @Validated
//public class EmployeeServiceImpl implements EmployeeService, UserDetailsService {
//
//    private final EmployeeRepository employeeRepository;
//    private final TenantSchemaUtil tenantSchemaUtil;
//    private final PasswordEncoder passwordEncoder;
//
//    public EmployeeServiceImpl(EmployeeRepository employeeRepository, TenantSchemaUtil tenantSchemaUtil, PasswordEncoder passwordEncoder) {
//        this.employeeRepository = employeeRepository;
//        this.tenantSchemaUtil = tenantSchemaUtil;
//        this.passwordEncoder = passwordEncoder;
//    }
//
////    @Override
////    public UserDetails loadUserByUsername(String email) throws InvalidEmailException {
////        ensureSchemaSwitch();
////        Employee employee = employeeRepository.findByEmail(email)
////                .orElseThrow(() -> new InvalidEmailException("Employee not found with email: " + email));
////        return new org.springframework.security.core.userdetails.User(
////                employee.getEmail(),
////                employee.getPassword(),
////                Collections.singleton(new SimpleGrantedAuthority(employee.getRole().getRoleName()))
////        );
////    }
//@Override
//public UserDetails loadUserByUsername(String email) throws InvalidEmailException {
//    ensureSchemaSwitch();
//    Employee employee = employeeRepository.findByEmail(email)
//            .orElseThrow(() -> new InvalidEmailException("Employee not found with email: " + email));
//
//    // Multi-role support: collect all authorities
//    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
//    if (employee.getRoles() != null) {
//        for (Role role : employee.getRoles()) {
//            if (role != null && role.getRoleName() != null) {
//                authorities.add(new SimpleGrantedAuthority(role.getRoleName()));
//            }
//        }
//    }
//
//    return new org.springframework.security.core.userdetails.User(
//            employee.getEmail(),
//            employee.getPassword(),
//            authorities
//    );
//}
//
//    @Transactional
//    @Override
//    public Employee createEmployee(@Valid Employee employee) { // Added @Valid for bean validation
//        ensureSchemaSwitch();
//        // Ensure password is encoded if not already
//        if (employee.getPassword() != null && !employee.getPassword().startsWith("$2a$")) { // Basic check
//            employee.setPassword(passwordEncoder.encode(employee.getPassword()));
//        }
//        // Ensure cascaded entities are handled if they are new
//        prepareCascadedEntities(employee);
//        return employeeRepository.save(employee);
//    }
//
//    /**
//     * ✅ Create multiple new Employees.
//     * Each employee in the list should be validated (e.g., against OnEmployeeProfile group).
//     */
//    @Transactional
//    @Override
//    // If you want to validate each employee in the list with a specific group,
//    // you'd typically do it in the controller or by iterating here with a Validator instance.
//    // @Validated(OnEmployeeProfile.class) // This on the method might not work directly on List<Employee> elements.
//    public List<Employee> createMultipleEmployees(List<@Valid Employee> employees) { // @Valid on List element
//        ensureSchemaSwitch();
//        List<Employee> savedEmployees = new ArrayList<>();
//        for (Employee employee : employees) {
//            // Ensure password is encoded if not already
//            if (employee.getPassword() != null && !employee.getPassword().startsWith("$2a$")) { // Basic check
//                employee.setPassword(passwordEncoder.encode(employee.getPassword()));
//            }
//            // Ensure tenantId is set (can be derived from context if not set on each employee object)
//            if (employee.getTenantId() == null) {
//                employee.setTenantId(TenantContext.getTenant());
//            }
//            // Ensure cascaded entities are properly initialized if necessary
//            prepareCascadedEntities(employee);
//
//            // Here you could add explicit validation for each employee if needed:
//            // Set<ConstraintViolation<Employee>> violations = validator.validate(employee, OnEmployeeProfile.class);
//            // if (!violations.isEmpty()) {
//            //     throw new ConstraintViolationException("Validation failed for employee: " + employee.getEmployeeId(), violations);
//            // }
//            savedEmployees.add(employeeRepository.save(employee));
//        }
//        return savedEmployees;
//        // For better performance on very large lists, consider employeeRepository.saveAll(preparedEmployees);
//        // But saveAll might have trickier error handling for individual validation failures.
//    }
//
//
//    /**
//     * Helper method to prepare cascaded entities if they are being newly created
//     * along with the Employee. This is more relevant if the input Employee objects
//     * might not have fully fleshed-out child entities.
//     */
//    private void prepareCascadedEntities(Employee employee) {
//        if (employee.getPersonalInfo() == null) {
//            // If PersonalInfo is always expected, this might indicate an issue
//            // or you might want to initialize a default one.
//            // For bulk creation, it's often assumed DTOs/input objects are complete.
//            // If creating from minimal data, logic similar to SubscriptionServiceImpl would be needed.
//            // For this generic method, we assume PersonalInfo is provided if required by validation.
//        }
//
//        if (employee.getOrganizationalInfo() == null) {
//            // Similar to PersonalInfo
//        } else {
//            if (employee.getOrganizationalInfo().getEmploymentDetails() == null) {
//                // Initialize with defaults if this is a bulk "quick-add" scenario
//                // and defaults are acceptable. Otherwise, rely on input validity.
//                // Example (use with caution, ensure this matches business logic):
//                // EmploymentDetails ed = new EmploymentDetails();
//                // ed.setDateOfJoining(LocalDate.now());
//                // ed.setEmploymentType(EmploymentType.PERMANENT); // Default
//                // ed.setEmploymentStatus(EmploymentStatus.ACTIVE);
//                // ed.setNoticePeriodDays(0); // Default
//                // ed.setWorkMode(WorkMode.WORK_FROM_OFFICE); // Default
//                // employee.getOrganizationalInfo().setEmploymentDetails(ed);
//            }
//            if (employee.getOrganizationalInfo().getJobContextDetails() == null) {
//                // Similar initialization logic if needed
//                // JobContextDetails jcd = new JobContextDetails();
//                // jcd.setDepartmentName("Default"); // Default
//                // jcd.setJobGradeBand("Default"); // Default
//                // jcd.setCostCenter("Default"); // Default
//                // employee.getOrganizationalInfo().setJobContextDetails(jcd);
//            }
//            if (employee.getOrganizationalInfo().getOrgAssignmentEffectiveDate() == null) {
//                // employee.getOrganizationalInfo().setOrgAssignmentEffectiveDate(LocalDate.now());
//            }
//        }
//    }
//
//
//    @Override
//    public Optional<Employee> getEmployeeByEmail(String email) {
//        ensureSchemaSwitch();
//        return employeeRepository.findByEmail(email);
//    }
//
//    @Override
//    public List<Employee> getAllEmployees() {
//        ensureSchemaSwitch();
//        return employeeRepository.findAll();
//    }
//    @Override
//    public List<String> getAllEmployeeIds() {
//        ensureSchemaSwitch();
//        return employeeRepository.findAll().stream()
//                .map(Employee::getEmployeeId)
//                .collect(Collectors.toList());
//    }
//
////    @Transactional
////    @Override
////    public Employee updateEmployee(String email, @Valid Employee updatedEmployee) { // Added @Valid
////        ensureSchemaSwitch();
////        Employee existingEmployee = employeeRepository.findByEmail(email)
////                .orElseThrow(() -> new RuntimeException("❌ Employee not found: " + email));
////
////        // Smartly update fields from updatedEmployee to existingEmployee
////        // This avoids nullifying fields not present in the payload and handles child entities.
////        updateExistingEmployeeData(existingEmployee, updatedEmployee);
////
////        return employeeRepository.save(existingEmployee);
////    }
////
////    private void updateExistingEmployeeData(Employee existing, Employee incoming) {
////        // Update direct Employee fields
////        if (incoming.getEmployeeId() != null) existing.setEmployeeId(incoming.getEmployeeId());
////        // Email is typically not changed as it's an identifier, but if allowed:
////        // if (incoming.getEmail() != null) existing.setEmail(incoming.getEmail());
////        if (incoming.getPhoneNumber() != null) existing.setPhoneNumber(incoming.getPhoneNumber());
////        if (incoming.getRole() != null) existing.setRole(incoming.getRole());
////        // Password should be handled via a separate "changePassword" flow for security
////        // if (incoming.getPassword() != null && !incoming.getPassword().isEmpty()) {
////        //    existing.setPassword(passwordEncoder.encode(incoming.getPassword()));
////        // }
////
////        // Update PersonalInfo
////        if (incoming.getPersonalInfo() != null) {
////            if (existing.getPersonalInfo() == null) existing.setPersonalInfo(new PersonalInfo());
////            PersonalInfo exPI = existing.getPersonalInfo();
////            PersonalInfo incPI = incoming.getPersonalInfo();
////            if (incPI.getFirstName() != null) exPI.setFirstName(incPI.getFirstName());
////            if (incPI.getMiddleName() != null) exPI.setMiddleName(incPI.getMiddleName());
////            if (incPI.getLastName() != null) exPI.setLastName(incPI.getLastName());
////            if (incPI.getGender() != null) exPI.setGender(incPI.getGender());
////            if (incPI.getDateOfBirth() != null) exPI.setDateOfBirth(incPI.getDateOfBirth());
////            // ... copy all other updatable PersonalInfo fields
////        }
////
////        // Update OrganizationalInfo and its children
////        if (incoming.getOrganizationalInfo() != null) {
////            if (existing.getOrganizationalInfo() == null) existing.setOrganizationalInfo(new OrganizationalInfo());
////            OrganizationalInfo exOI = existing.getOrganizationalInfo();
////            OrganizationalInfo incOI = incoming.getOrganizationalInfo();
////
////            if (incOI.getOrgAssignmentEffectiveDate() != null) exOI.setOrgAssignmentEffectiveDate(incOI.getOrgAssignmentEffectiveDate());
////
////            if (incOI.getEmploymentDetails() != null) {
////                if (exOI.getEmploymentDetails() == null) exOI.setEmploymentDetails(new EmploymentDetails());
////                EmploymentDetails exED = exOI.getEmploymentDetails();
////                EmploymentDetails incED = incOI.getEmploymentDetails();
////                if (incED.getDateOfJoining() != null) exED.setDateOfJoining(incED.getDateOfJoining());
////                if (incED.getEmploymentType() != null) exED.setEmploymentType(incED.getEmploymentType());
////                // ... copy all other updatable EmploymentDetails fields
////            }
////
////            if (incOI.getJobContextDetails() != null) {
////                if (exOI.getJobContextDetails() == null) exOI.setJobContextDetails(new JobContextDetails());
////                JobContextDetails exJCD = exOI.getJobContextDetails();
////                JobContextDetails incJCD = incOI.getJobContextDetails();
////                if (incJCD.getDepartmentName() != null) exJCD.setDepartmentName(incJCD.getDepartmentName());
////                // ... copy all other updatable JobContextDetails fields
////            }
////        }
////        // Update work structure assignments if provided
////        if (incoming.getWorkLocation() != null) existing.setWorkLocation(incoming.getWorkLocation());
////        if (incoming.getBusinessUnit() != null) existing.setBusinessUnit(incoming.getBusinessUnit());
////        if (incoming.getJobTitle() != null) existing.setJobTitle(incoming.getJobTitle());
////        if (incoming.getReportingManager() != null) existing.setReportingManager(incoming.getReportingManager());
////        if (incoming.getHrManager() != null) existing.setHrManager(incoming.getHrManager());
////    }
//
//
//    @Transactional
//    @Override
//    public void deleteEmployee(String email) {
//        ensureSchemaSwitch();
//        Employee employee = employeeRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("❌ Employee not found: " + email));
//        employeeRepository.delete(employee);
//    }
//
//    private void ensureSchemaSwitch() {
//        tenantSchemaUtil.ensureTenantSchemaIsSet();
//    }
//
//    @Override
//    public Optional<Employee> getEmployeeByEmployeeId(String employeeId) {
//        ensureSchemaSwitch();
//        return employeeRepository.findByEmployeeId(employeeId);
//    }
//
//}