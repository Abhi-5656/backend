package com.wfm.experts.tenant.common.employees.service.impl;

import com.wfm.experts.exception.InvalidEmailException;
import com.wfm.experts.repository.tenant.common.EmployeeRepository;
import com.wfm.experts.tenant.common.employees.dto.EmployeeDTO;
import com.wfm.experts.tenant.common.employees.entity.Employee;
import com.wfm.experts.tenant.common.employees.entity.Role;
import com.wfm.experts.tenant.common.employees.mapper.EmployeeMapper;
import com.wfm.experts.tenant.common.employees.service.EmployeeService;
import com.wfm.experts.tenancy.TenantContext;
import com.wfm.experts.util.TenantSchemaUtil;
import jakarta.validation.Valid;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *Implements `UserDetailsService` for Spring Security & CRUD operations for Employees.
 */
@Service
public class EmployeeServiceImpl implements EmployeeService, UserDetailsService {

    private final EmployeeRepository employeeRepository;
    private final TenantSchemaUtil tenantSchemaUtil;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeMapper employeeMapper;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, TenantSchemaUtil tenantSchemaUtil, PasswordEncoder passwordEncoder, EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.tenantSchemaUtil = tenantSchemaUtil;
        this.passwordEncoder = passwordEncoder;
        this.employeeMapper = employeeMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws InvalidEmailException {
        ensureSchemaSwitch();
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidEmailException("Employee not found with email: " + email));

        // Multi-role support: collect all authorities
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (employee.getRoles() != null) {
            for (Role role : employee.getRoles()) {
                if (role != null && role.getRoleName() != null) {
                    authorities.add(new SimpleGrantedAuthority(role.getRoleName()));
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
        Employee employee = employeeMapper.toEntity(employeeDTO);
        // Ensure password is encoded if not already
        if (employee.getPassword() != null && !employee.getPassword().startsWith("$2a$")) { // Basic check
            employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        }
        // Ensure cascaded entities are handled if they are new
        prepareCascadedEntities(employee);
        Employee savedEmployee = employeeRepository.save(employee);
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
        for (EmployeeDTO employeeDTO : employeeDTOs) {
            Employee employee = employeeMapper.toEntity(employeeDTO);
            // Ensure password is encoded if not already
            if (employee.getPassword() != null && !employee.getPassword().startsWith("$2a$")) { // Basic check
                employee.setPassword(passwordEncoder.encode(employee.getPassword()));
            }
            // Ensure tenantId is set (can be derived from context if not set on each employee object)
            if (employee.getTenantId() == null) {
                employee.setTenantId(TenantContext.getTenant());
            }
            // Ensure cascaded entities are properly initialized if necessary
            prepareCascadedEntities(employee);

            employeesToSave.add(employee);
        }
        List<Employee> savedEmployees = employeeRepository.saveAll(employeesToSave);
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
                .orElseThrow(() -> new RuntimeException("❌ Employee not found: " + email));

        // Smartly update fields from updatedEmployee to existingEmployee
        updateExistingEmployeeData(existingEmployee, updatedEmployeeDTO);

        Employee savedEmployee = employeeRepository.save(existingEmployee);
        return employeeMapper.toDto(savedEmployee);
    }

    private void updateExistingEmployeeData(Employee existing, EmployeeDTO incoming) {
        // Update direct Employee fields
        if (incoming.getEmployeeId() != null) existing.setEmployeeId(incoming.getEmployeeId());
        if (incoming.getPhoneNumber() != null) existing.setPhoneNumber(incoming.getPhoneNumber());

        // Update PersonalInfo
        if (incoming.getPersonalInfo() != null) {
            if (existing.getPersonalInfo() == null) {
                existing.setPersonalInfo(new com.wfm.experts.tenant.common.employees.entity.PersonalInfo());
            }
            // You can use a dedicated mapper for updates or manually set fields
            // For simplicity, manual updates are shown here
            com.wfm.experts.tenant.common.employees.dto.PersonalInfoDTO incPI = incoming.getPersonalInfo();
            com.wfm.experts.tenant.common.employees.entity.PersonalInfo exPI = existing.getPersonalInfo();
            if (incPI.getFirstName() != null) exPI.setFirstName(incPI.getFirstName());
            if (incPI.getLastName() != null) exPI.setLastName(incPI.getLastName());
            // ... copy other updatable PersonalInfo fields
        }

        // Update OrganizationalInfo and its children
        if (incoming.getOrganizationalInfo() != null) {
            if (existing.getOrganizationalInfo() == null) {
                existing.setOrganizationalInfo(new com.wfm.experts.tenant.common.employees.entity.OrganizationalInfo());
            }
            // Similarly, update fields for OrganizationalInfo, EmploymentDetails, JobContextDetails
        }
        // Update work structure assignments if provided
        // You'll need to fetch the entities based on the IDs from the DTOs
    }

    @Transactional
    @Override
    public void deleteEmployee(String email) {
        ensureSchemaSwitch();
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("❌ Employee not found: " + email));
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
}