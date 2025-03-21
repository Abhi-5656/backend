package com.wfm.experts.config;

import com.wfm.experts.security.JwtAuthenticationFilter;
import com.wfm.experts.service.EmployeeService;
import com.wfm.experts.tenancy.TenantInterceptor;
import com.wfm.experts.tenancy.TenantRewriteFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.context.annotation.Lazy; // ✅ Import @Lazy

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final EmployeeService employeeService;
    private final TenantRewriteFilter tenantRewriteFilter;

    public SecurityConfig(
            @Lazy JwtAuthenticationFilter jwtAuthenticationFilter, // ✅ Use @Lazy to break cycle
            TenantInterceptor tenantInterceptor,
            EmployeeService employeeService, TenantInterceptor tenantInterceptor1, TenantRewriteFilter tenantRewriteFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.employeeService = employeeService;

        this.tenantRewriteFilter = tenantRewriteFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return employeeService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/subscriptions/**").permitAll() // Public endpoints allowed
                        .anyRequest().authenticated()  // Require authentication for other requests
                )
                .addFilterBefore(tenantRewriteFilter, UsernamePasswordAuthenticationFilter.class) // Path cleanup filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // JWT filter
                .build();
    }


}
