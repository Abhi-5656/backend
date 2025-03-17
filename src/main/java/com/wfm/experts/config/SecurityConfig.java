package com.wfm.experts.config;

import com.wfm.experts.security.CustomUserDetailsService;
import com.wfm.experts.security.JwtAuthenticationFilter;
import com.wfm.experts.security.JwtUtil;
import com.wfm.experts.repository.tenant.common.EmployeeRepository;
import com.wfm.experts.util.TenantSchemaUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * ✅ Security Configuration for JWT Authentication and API Security
 */
@Configuration
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final EmployeeRepository employeeRepository;
    private final TenantSchemaUtil tenantSchemaUtil;

    public SecurityConfig(JwtUtil jwtUtil, EmployeeRepository employeeRepository, TenantSchemaUtil tenantSchemaUtil) {
        this.jwtUtil = jwtUtil;
        this.employeeRepository = employeeRepository;
        this.tenantSchemaUtil = tenantSchemaUtil;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/subscriptions/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // ✅ Add JWT Filter

        return http.build();
    }

    /**
     * ✅ Register `JwtAuthenticationFilter` as a Bean
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(UserDetailsService userDetailsService, AuthenticationManager authenticationManager) {
        return new JwtAuthenticationFilter(jwtUtil, userDetailsService,authenticationManager, tenantSchemaUtil);
    }

    /**
     * ✅ Define `UserDetailsService` with All Required Dependencies
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return new CustomUserDetailsService(employeeRepository, jwtUtil, tenantSchemaUtil);
    }

    /**
     * ✅ Password Encoder for Secure User Authentication
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * ✅ Provide `AuthenticationManager` Bean
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
