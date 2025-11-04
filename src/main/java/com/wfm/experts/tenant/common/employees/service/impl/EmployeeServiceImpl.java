package com.wfm.experts.tenant.common.employees.service.impl;

import com.wfm.experts.exception.InvalidEmailException;
import com.wfm.experts.tenant.common.employees.exception.EmployeeServiceException;
import com.wfm.experts.tenant.common.employees.repository.EmployeeRepository;
import com.wfm.experts.setup.roles.repository.RoleRepository;
import com.wfm.experts.tenant.common.employees.dto.EmployeeDTO;
import com.wfm.experts.tenant.common.employees.entity.Employee;
import com.wfm.experts.setup.roles.entity.Role;
// --- NEW IMPORTS ---
import com.wfm.experts.tenant.common.employees.exception.EmployeeAlreadyExistsException;
import com.wfm.experts.tenant.common.employees.exception.ResourceNotFoundException;
// --- END NEW IMPORTS ---
import com.wfm.experts.tenant.common.employees.mapper.EmployeeMapper;
import com.wfm.experts.tenant.common.employees.service.EmployeeService;
import com.wfm.experts.tenancy.TenantContext;
import com.wfm.experts.util.TenantIdUtil;
import com.wfm.experts.util.TenantSchemaUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import com.wfm.experts.notificationengine.service.NotificationOrchestrationService;
import com.wfm.experts.notificationengine.dto.NotificationRequest;
import org.springframework.core.env.Environment;

/**
 *Implements `UserDetailsService` for Spring Security & CRUD operations for Employees.
 */
@Service
public class EmployeeServiceImpl implements EmployeeService, UserDetailsService {

    private final EmployeeRepository employeeRepository;
    private final TenantSchemaUtil tenantSchemaUtil;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeMapper employeeMapper;
    private final RoleRepository roleRepository;
    private final NotificationOrchestrationService notificationOrchestrationService;
    private final Environment environment;
    @Value("${frontend.base-url}")
    private String frontendBaseUrl;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, TenantSchemaUtil tenantSchemaUtil, @Lazy PasswordEncoder passwordEncoder, EmployeeMapper employeeMapper, RoleRepository roleRepository, NotificationOrchestrationService notificationOrchestrationService, Environment environment) {
        this.employeeRepository = employeeRepository;
        this.tenantSchemaUtil = tenantSchemaUtil;
        this.passwordEncoder = passwordEncoder;
        this.employeeMapper = employeeMapper;
        this.roleRepository = roleRepository;
        this.notificationOrchestrationService = notificationOrchestrationService;
        this.environment = environment;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws InvalidEmailException {
        ensureSchemaSwitch();
        // This is part of AUTH flow, so InvalidEmailException (which maps to 401/404 via AuthExceptionHandler) is correct.
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidEmailException("Employee not found with email: " + email));

        // Multi-role support: collect all authorities
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (employee.getRoles() != null) {
            for (Role role : employee.getRoles()) {
                if (role != null && role.getRoleName() != null) {
                    authorities.add(new SimpleGrantedAuthority(role.getRoleName()));
                    role.getPermissions().forEach(p -> authorities.add(new SimpleGrantedAuthority(p.getName())));
                }
            }
        }

        return new org.springframework.security.core.userdetails.User(
                employee.getEmail(),
                employee.getPassword(),
                authorities
        );
    }

    @Transactional
    @Override
    public EmployeeDTO createEmployee(@Valid EmployeeDTO employeeDTO) {
        ensureSchemaSwitch();

        // --- NEW VALIDATION ---
        if (employeeRepository.existsByEmail(employeeDTO.getEmail())) {
            throw new EmployeeAlreadyExistsException("Email", employeeDTO.getEmail());
        }
        if (employeeRepository.existsByEmployeeId(employeeDTO.getEmployeeId())) {
            throw new EmployeeAlreadyExistsException("Employee ID", employeeDTO.getEmployeeId());
        }
        if (employeeRepository.existsByPhoneNumber(employeeDTO.getPhoneNumber())) {
            throw new EmployeeAlreadyExistsException("Phone Number", employeeDTO.getPhoneNumber());
        }
        // --- END NEW VALIDATION ---

        Employee employee = employeeMapper.toEntity(employeeDTO);

        // FIX: Map roles from DTO to entity
        if (employeeDTO.getRoles() != null && !employeeDTO.getRoles().isEmpty()) {
            List<Role> roles = employeeDTO.getRoles().stream()
                    .map(roleName -> roleRepository.findByRoleName(roleName)
                            // --- MODIFIED EXCEPTION ---
                            .orElseThrow(() -> new ResourceNotFoundException("Role", roleName)))
                    .collect(Collectors.toList());
            employee.setRoles(roles);
        }

        String rawPassword;
        // Ensure password is encoded if not already
        if (employee.getPassword() != null && !employee.getPassword().startsWith("$2a$")) { // Basic check
            rawPassword = employee.getPassword();
            employee.setPassword(passwordEncoder.encode(rawPassword));
        } else {
            // Generate a random password if one isn't provided
            rawPassword = generateRandomPassword();
            // *** PRINTING TO CONSOLE AS REQUESTED ***
            System.out.println("Creating user. Email: " + employee.getEmail() + ", Generated Raw Password: " + rawPassword);
            employee.setPassword(passwordEncoder.encode(rawPassword));
        }
        // Ensure cascaded entities are handled if they are new
        prepareCascadedEntities(employee);
        Employee savedEmployee = employeeRepository.save(employee);

        // [NEW FUNCTIONALITY] Send welcome email after successful employee creation
        sendWelcomeEmail(savedEmployee, rawPassword);

        return employeeMapper.toDto(savedEmployee);
    }

    /**
     * ✅ Create multiple new Employees.
     * Each employee in the list should be validated (e.g., against OnEmployeeProfile group).
     */
    @Transactional
    @Override
    public List<EmployeeDTO> createMultipleEmployees(List<@Valid EmployeeDTO> employeeDTOs) {
        ensureSchemaSwitch();
        List<Employee> employeesToSave = new ArrayList<>();
        // Temporary map to hold raw passwords for emails
        Map<String, String> rawPasswords = new HashMap<>();

        for (EmployeeDTO employeeDTO : employeeDTOs) {
            // --- NEW VALIDATION ---
            if (employeeRepository.existsByEmail(employeeDTO.getEmail())) {
                throw new EmployeeAlreadyExistsException("Email", employeeDTO.getEmail());
            }
            if (employeeRepository.existsByEmployeeId(employeeDTO.getEmployeeId())) {
                throw new EmployeeAlreadyExistsException("Employee ID", employeeDTO.getEmployeeId());
            }
            if (employeeRepository.existsByPhoneNumber(employeeDTO.getPhoneNumber())) {
                throw new EmployeeAlreadyExistsException("Phone Number", employeeDTO.getPhoneNumber());
            }
            // --- END NEW VALIDATION ---

            Employee employee = employeeMapper.toEntity(employeeDTO);

            // FIX: Map roles from DTO to entity
            if (employeeDTO.getRoles() != null && !employeeDTO.getRoles().isEmpty()) {
                List<Role> roles = employeeDTO.getRoles().stream()
                        .map(roleName -> roleRepository.findByRoleName(roleName)
                                // --- MODIFIED EXCEPTION ---
                                .orElseThrow(() -> new ResourceNotFoundException("Role", roleName)))
                        .collect(Collectors.toList());
                employee.setRoles(roles);
            }

            // Generate a random password for each new employee
            String rawPassword = generateRandomPassword();
            rawPasswords.put(employee.getEmail(), rawPassword);
            // *** PRINTING TO CONSOLE AS REQUESTED ***
            System.out.println("Creating user. Email: " + employeeDTO.getEmail() + ", Generated Raw Password: " + rawPassword);
            employee.setPassword(passwordEncoder.encode(rawPassword));

            // Ensure tenantId is set (can be derived from context if not set on each employee object)
            if (employee.getTenantId() == null) {
                employee.setTenantId(TenantContext.getTenant());
            }
            // Ensure cascaded entities are properly initialized if necessary
            prepareCascadedEntities(employee);

            employeesToSave.add(employee);
        }
        List<Employee> savedEmployees = employeeRepository.saveAll(employeesToSave);

        // [NEW FUNCTIONALITY] Send welcome email for all newly created employees
        for (Employee savedEmployee : savedEmployees) {
            String rawPassword = rawPasswords.get(savedEmployee.getEmail());
            if (rawPassword != null) {
                sendWelcomeEmail(savedEmployee, rawPassword);
            }
        }

        return savedEmployees.stream()
                .map(employeeMapper::toDto)
                .collect(Collectors.toList());
    }


    /**
     * Helper method to prepare cascaded entities if they are being newly created
     * along with the Employee. This is more relevant if the input Employee objects
     * might not have fully fleshed-out child entities.
     */
    private void prepareCascadedEntities(Employee employee) {
        if (employee.getPersonalInfo() == null) {
            // If PersonalInfo is always expected, this might indicate an issue
            // or you might want to initialize a default one.
            // For bulk creation, it's often assumed DTOs/input objects are complete.
            // If creating from minimal data, logic similar to SubscriptionServiceImpl would be needed.
            // For this generic method, we assume PersonalInfo is provided if required by validation.
        }

        if (employee.getOrganizationalInfo() == null) {
            // Similar to PersonalInfo
        } else {
            if (employee.getOrganizationalInfo().getEmploymentDetails() == null) {
                // Initialize with defaults if this is a bulk "quick-add" scenario
                // and defaults are acceptable. Otherwise, rely on input validity.
            }
            if (employee.getOrganizationalInfo().getJobContextDetails() == null) {
                // Similar initialization logic if needed
            }
            if (employee.getOrganizationalInfo().getOrgAssignmentEffectiveDate() == null) {
                // employee.getOrganizationalInfo().setOrgAssignmentEffectiveDate(LocalDate.now());
            }
        }
    }


    @Override
    public Optional<EmployeeDTO> getEmployeeByEmail(String email) {
        ensureSchemaSwitch();
        return employeeRepository.findByEmail(email)
                .map(employeeMapper::toDto);
    }

    @Override
    public List<EmployeeDTO> getAllEmployees() {
        ensureSchemaSwitch();
        List<Employee> employees = employeeRepository.findAll();
        return employees.stream()
                .map(employeeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllEmployeeIds() {
        ensureSchemaSwitch();
        return employeeRepository.findAll().stream()
                .map(Employee::getEmployeeId)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public EmployeeDTO updateEmployee(String email, @Valid EmployeeDTO updatedEmployeeDTO) {
        ensureSchemaSwitch();
        Employee existingEmployee = employeeRepository.findByEmail(email)
                // --- MODIFIED EXCEPTION ---
                .orElseThrow(() -> new ResourceNotFoundException("Employee", email));

        // Smartly update fields from updatedEmployee to existingEmployee
        updateExistingEmployeeData(existingEmployee, updatedEmployeeDTO);

        Employee savedEmployee = employeeRepository.save(existingEmployee);
        return employeeMapper.toDto(savedEmployee);
    }

    @Transactional
    @Override
    public List<EmployeeDTO> updateMultipleEmployees(List<@Valid EmployeeDTO> employeeDTOs) {
        ensureSchemaSwitch();
        List<Employee> employeesToUpdate = new ArrayList<>();
        for (EmployeeDTO employeeDTO : employeeDTOs) {
            Employee existingEmployee = employeeRepository.findByEmail(employeeDTO.getEmail())
                    // --- MODIFIED EXCEPTION ---
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", employeeDTO.getEmail()));

            updateExistingEmployeeData(existingEmployee, employeeDTO);
            employeesToUpdate.add(existingEmployee);
        }

        List<Employee> updatedEmployees = employeeRepository.saveAll(employeesToUpdate);
        return updatedEmployees.stream()
                .map(employeeMapper::toDto)
                .collect(Collectors.toList());
    }

    private void updateExistingEmployeeData(Employee existing, EmployeeDTO incoming) {
        // Update direct Employee fields
        if (incoming.getEmployeeId() != null && !existing.getEmployeeId().equals(incoming.getEmployeeId())) {
            // --- NEW VALIDATION ---
            if(employeeRepository.existsByEmployeeId(incoming.getEmployeeId())) {
                throw new EmployeeAlreadyExistsException("Employee ID", incoming.getEmployeeId());
            }
            existing.setEmployeeId(incoming.getEmployeeId());
        }
        if (incoming.getPhoneNumber() != null && !existing.getPhoneNumber().equals(incoming.getPhoneNumber())) {
            // --- NEW VALIDATION ---
            if(employeeRepository.existsByPhoneNumber(incoming.getPhoneNumber())) {
                throw new EmployeeAlreadyExistsException("Phone Number", incoming.getPhoneNumber());
            }
            existing.setPhoneNumber(incoming.getPhoneNumber());
        }

        if (incoming.getPassword() != null && !incoming.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(incoming.getPassword()));
        }

        if (incoming.getRoles() != null && !incoming.getRoles().isEmpty()) {
            List<Role> roles = incoming.getRoles().stream()
                    .map(roleName -> roleRepository.findByRoleName(roleName)
                            // --- MODIFIED EXCEPTION ---
                            .orElseThrow(() -> new ResourceNotFoundException("Role", roleName)))
                    .collect(Collectors.toList());
            existing.setRoles(roles);
        }

        if (incoming.getReportingManagerId() != null) {
            if (incoming.getReportingManagerId().equals(existing.getEmployeeId())) {
                throw new EmployeeServiceException("Employee cannot be their own reporting manager.");
            }
            Employee manager = employeeRepository.findByEmployeeId(incoming.getReportingManagerId())
                    // --- MODIFIED EXCEPTION ---
                    .orElseThrow(() -> new ResourceNotFoundException("Reporting Manager", incoming.getReportingManagerId()));
            existing.setReportingManager(manager);
        } else {
            existing.setReportingManager(null); // Allow unsetting manager
        }

        if (incoming.getHrManagerId() != null) {
            if (incoming.getHrManagerId().equals(existing.getEmployeeId())) {
                throw new EmployeeServiceException("Employee cannot be their own HR manager.");
            }
            Employee hrManager = employeeRepository.findByEmployeeId(incoming.getHrManagerId())
                    // --- MODIFIED EXCEPTION ---
                    .orElseThrow(() -> new ResourceNotFoundException("HR Manager", incoming.getHrManagerId()));
            existing.setHrManager(hrManager);
        } else {
            existing.setHrManager(null); // Allow unsetting HR manager
        }


        // Update PersonalInfo
        if (incoming.getPersonalInfo() != null) {
            if (existing.getPersonalInfo() == null) {
                existing.setPersonalInfo(new com.wfm.experts.tenant.common.employees.entity.PersonalInfo());
            }
            com.wfm.experts.tenant.common.employees.dto.PersonalInfoDTO incPI = incoming.getPersonalInfo();
            com.wfm.experts.tenant.common.employees.entity.PersonalInfo exPI = existing.getPersonalInfo();
            if (incPI.getFirstName() != null) {
                exPI.setFirstName(incPI.getFirstName());
            }
            if (incPI.getMiddleName() != null) {
                exPI.setMiddleName(incPI.getMiddleName());
            }
            if (incPI.getLastName() != null) {
                exPI.setLastName(incPI.getLastName());
            }
            if (incPI.getFullName() != null) {
                exPI.setFullName(incPI.getFullName());
            }
            if (incPI.getDisplayName() != null) {
                exPI.setDisplayName(incPI.getDisplayName());
            }
            if (incPI.getGender() != null) {
                exPI.setGender(incPI.getGender());
            }
            if (incPI.getDateOfBirth() != null) {
                exPI.setDateOfBirth(incPI.getDateOfBirth());
            }
            if (incPI.getBloodGroup() != null) {
                exPI.setBloodGroup(incPI.getBloodGroup());
            }
            if (incPI.getMaritalStatus() != null) {
                exPI.setMaritalStatus(incPI.getMaritalStatus());
            }
            if (incPI.getPanNumber() != null) {
                exPI.setPanNumber(incPI.getPanNumber());
            }
            if (incPI.getAadhaarNumber() != null) {
                exPI.setAadhaarNumber(incPI.getAadhaarNumber());
            }
            if (incPI.getNationality() != null) {
                exPI.setNationality(incPI.getNationality());
            }
            if (incPI.getPersonalEmail() != null) {
                exPI.setPersonalEmail(incPI.getPersonalEmail());
            }
            if (incPI.getAlternateMobile() != null) {
                exPI.setAlternateMobile(incPI.getAlternateMobile());
            }
            if (incPI.getEmergencyContact() != null) {
                if (exPI.getEmergencyContact() == null) {
                    exPI.setEmergencyContact(new com.wfm.experts.tenant.common.employees.entity.EmergencyContact());
                }
                if (incPI.getEmergencyContact().getContactName() != null) {
                    exPI.getEmergencyContact().setContactName(incPI.getEmergencyContact().getContactName());
                }
                if (incPI.getEmergencyContact().getContactNumber() != null) {
                    exPI.getEmergencyContact().setContactNumber(incPI.getEmergencyContact().getContactNumber());
                }
                if (incPI.getEmergencyContact().getRelationship() != null) {
                    exPI.getEmergencyContact().setRelationship(incPI.getEmergencyContact().getRelationship());
                }
            }
            if (incPI.getCurrentAddress() != null) {
                if (exPI.getCurrentAddress() == null) {
                    exPI.setCurrentAddress(new com.wfm.experts.tenant.common.employees.entity.Address());
                }
                if (incPI.getCurrentAddress().getAddressLine1() != null) {
                    exPI.getCurrentAddress().setAddressLine1(incPI.getCurrentAddress().getAddressLine1());
                }
                if (incPI.getCurrentAddress().getAddressLine2() != null) {
                    exPI.getCurrentAddress().setAddressLine2(incPI.getCurrentAddress().getAddressLine2());
                }
                if (incPI.getCurrentAddress().getCity() != null) {
                    exPI.getCurrentAddress().setCity(incPI.getCurrentAddress().getCity());
                }
                if (incPI.getCurrentAddress().getState() != null) {
                    exPI.getCurrentAddress().setState(incPI.getCurrentAddress().getState());
                }
                if (incPI.getCurrentAddress().getPincode() != null) {
                    exPI.getCurrentAddress().setPincode(incPI.getCurrentAddress().getPincode());
                }
            }
            exPI.setPermanentSameAsCurrent(incPI.isPermanentSameAsCurrent());
            if (incPI.getPermanentAddress() != null) {
                if (exPI.getPermanentAddress() == null) {
                    exPI.setPermanentAddress(new com.wfm.experts.tenant.common.employees.entity.Address());
                }
                if (incPI.getPermanentAddress().getAddressLine1() != null) {
                    exPI.getPermanentAddress().setAddressLine1(incPI.getPermanentAddress().getAddressLine1());
                }
                if (incPI.getPermanentAddress().getAddressLine2() != null) {
                    exPI.getPermanentAddress().setAddressLine2(incPI.getPermanentAddress().getAddressLine2());
                }
                if (incPI.getPermanentAddress().getCity() != null) {
                    exPI.getPermanentAddress().setCity(incPI.getPermanentAddress().getCity());
                }
                if (incPI.getPermanentAddress().getState() != null) {
                    exPI.getPermanentAddress().setState(incPI.getPermanentAddress().getState());
                }
                if (incPI.getPermanentAddress().getPincode() != null) {
                    exPI.getPermanentAddress().setPincode(incPI.getPermanentAddress().getPincode());
                }
            }
        }

        // Update OrganizationalInfo and its children
        if (incoming.getOrganizationalInfo() != null) {
            if (existing.getOrganizationalInfo() == null) {
                existing.setOrganizationalInfo(new com.wfm.experts.tenant.common.employees.entity.OrganizationalInfo());
            }

            com.wfm.experts.tenant.common.employees.dto.OrganizationalInfoDTO incOI = incoming.getOrganizationalInfo();
            com.wfm.experts.tenant.common.employees.entity.OrganizationalInfo exOI = existing.getOrganizationalInfo();

            if (incOI.getOrgAssignmentEffectiveDate() != null) {
                exOI.setOrgAssignmentEffectiveDate(incOI.getOrgAssignmentEffectiveDate());
            }

            if (incOI.getEmploymentDetails() != null) {
                if (exOI.getEmploymentDetails() == null) {
                    exOI.setEmploymentDetails(new com.wfm.experts.tenant.common.employees.entity.EmploymentDetails());
                }
                com.wfm.experts.tenant.common.employees.dto.EmploymentDetailsDTO incED = incOI.getEmploymentDetails();
                com.wfm.experts.tenant.common.employees.entity.EmploymentDetails exED = exOI.getEmploymentDetails();

                if (incED.getDateOfJoining() != null) {
                    exED.setDateOfJoining(incED.getDateOfJoining());
                }
                if (incED.getEmploymentType() != null) {
                    exED.setEmploymentType(incED.getEmploymentType());
                }
                if (incED.getEmploymentStatus() != null) {
                    exED.setEmploymentStatus(incED.getEmploymentStatus());
                }
                if (incED.getNoticePeriodDays() != null) {
                    exED.setNoticePeriodDays(incED.getNoticePeriodDays());
                }
                if (incED.getWorkMode() != null) {
                    exED.setWorkMode(incED.getWorkMode());
                }
                if (incED.getConfirmationDate() != null) {
                    exED.setConfirmationDate(incED.getConfirmationDate());
                }
                if (incED.getProbationPeriodMonths() != null) {
                    exED.setProbationPeriodMonths(incED.getProbationPeriodMonths());
                }
            }

            if (incOI.getJobContextDetails() != null) {
                if (exOI.getJobContextDetails() == null) {
                    exOI.setJobContextDetails(new com.wfm.experts.tenant.common.employees.entity.JobContextDetails());
                }
                com.wfm.experts.tenant.common.employees.dto.JobContextDetailsDTO incJCD = incOI.getJobContextDetails();
                com.wfm.experts.tenant.common.employees.entity.JobContextDetails exJCD = exOI.getJobContextDetails();
                if (incJCD.getDepartmentName() != null) {
                    exJCD.setDepartmentName(incJCD.getDepartmentName());
                }
                if (incJCD.getJobGradeBand() != null) {
                    exJCD.setJobGradeBand(incJCD.getJobGradeBand());
                }
                if (incJCD.getCostCenter() != null) {
                    exJCD.setCostCenter(incJCD.getCostCenter());
                }
                if (incJCD.getOrganizationalRoleDescription() != null) {
                    exJCD.setOrganizationalRoleDescription(incJCD.getOrganizationalRoleDescription());
                }
            }
        }
    }

    @Transactional
    @Override
    public void deleteEmployee(String email) {
        ensureSchemaSwitch();
        Employee employee = employeeRepository.findByEmail(email)
                // --- MODIFIED EXCEPTION ---
                .orElseThrow(() -> new ResourceNotFoundException("Employee", email));
        employeeRepository.delete(employee);
    }

    private void ensureSchemaSwitch() {
        tenantSchemaUtil.ensureTenantSchemaIsSet();
    }



    @Override
    public Optional<EmployeeDTO> getEmployeeByEmployeeId(String employeeId) {
        ensureSchemaSwitch();
        return employeeRepository.findByEmployeeId(employeeId)
                .map(employeeMapper::toDto);
    }

    private String generateRandomPassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&!";
        int passwordLength = 12;
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(passwordLength);
        for (int i = 0; i < passwordLength; i++) {
            password.append(characters.charAt(random.nextInt(characters.length())));
        }
        return password.toString();
    }

    private void sendWelcomeEmail(Employee newEmployee, String rawPassword) {
        String tenantId = TenantContext.getTenant();
        if (tenantId == null) {
            System.err.println("Cannot send welcome email: Tenant ID is null in context.");
            return;
        }

        String employeeName = newEmployee.getPersonalInfo() != null && newEmployee.getPersonalInfo().getFullName() != null
                ? newEmployee.getPersonalInfo().getFullName()
                : newEmployee.getEmail();
        String companyName = TenantIdUtil.tenantIdToCompanyName(tenantId);

        try {
            // [FIX: Use frontendBaseUrl and append tenantId and /login]
            String loginUrl = String.format("%s/%s/login", frontendBaseUrl.replaceAll("/$", ""), tenantId);

            // Construct payload for template rendering
            Map<String, Object> payload = new HashMap<>();
            payload.put("userName", employeeName);
            payload.put("userEmail", newEmployee.getEmail());
            payload.put("generatedPassword", rawPassword);
            payload.put("loginUrl", loginUrl);
            payload.put("companyName", companyName);
            payload.put("currentYear", String.valueOf(java.time.LocalDate.now().getYear()));

            // The templateId relies on a migration script being run (e.g., V25)
            String templateId = "employee_welcome_email";

            com.wfm.experts.notificationengine.dto.NotificationRequest emailRequest = new com.wfm.experts.notificationengine.dto.NotificationRequest(
                    newEmployee.getEmployeeId(), // userId
                    com.wfm.experts.notificationengine.dto.NotificationRequest.ChannelType.EMAIL,
                    newEmployee.getEmail(),     // recipientAddress
                    templateId,
                    payload
            );

            notificationOrchestrationService.processNotificationRequest(emailRequest);
            System.out.println("Queued welcome email for: " + newEmployee.getEmail());

        } catch (Exception e) {
            System.err.println("Failed to send welcome email for " + newEmployee.getEmail() + ". Error: " + e.getMessage());
            e.printStackTrace();
            // Log the error but continue as this shouldn't fail the employee creation transaction
        }
    }
}