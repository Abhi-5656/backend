/*
 *
 *  * © 2024-2025 WFM EXPERTS INDIA PVT LTD. All rights reserved.
 *  *
 *  * This software, including all associated files, documentation, and related materials,
 *  * is the proprietary property of WFM EXPERTS INDIA PVT LTD. Unauthorized copying,
 *  * distribution, modification, or any form of use beyond the granted permissions
 *  * without prior written consent is strictly prohibited.
 *  *
 *  * DISCLAIMER:
 *  * This software is provided "as is," without warranty of any kind, express or implied,
 *  * including but not limited to the warranties of merchantability, fitness for a particular
 *  * purpose, and non-infringement.
 *  *
 *  * For inquiries, contact legal@wfmexperts.com.
 *
 */

package com.wfm.experts.config;

import com.wfm.experts.security.JwtAuthenticationFilter;
import com.wfm.experts.tenancy.TenantFilter;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final TenantFilter tenantFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, TenantFilter tenantFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.tenantFilter = tenantFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Password Encoder bean
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(withDefaults()) // ✅ Enable CORS
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",          // Authentication endpoints
                                "/api/subscriptions/**", // Subscription endpoints
                                "/public/**",            // Publicly accessible job application endpoints
                                "/ws/**",                // WebSocket handshake endpoint and its subpaths
                                "/swagger-ui/**",        // Swagger UI (optional)
                                "/v3/api-docs/**"      // OpenAPI docs (optional)
                        ).permitAll()
                        .anyRequest().authenticated() // All other requests require authentication
                )
                // TenantFilter runs before JwtAuthenticationFilter to set up TenantContext
                .addFilterBefore(tenantFilter, UsernamePasswordAuthenticationFilter.class)
                // JwtAuthenticationFilter runs after TenantFilter to handle JWT for HTTP requests
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration config = new CorsConfiguration();
//
//        config.setAllowedOrigins(List.of("http://localhost:4200")); // ✅ Frontend origin
//        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
//        config.setAllowCredentials(true); // Enable if needed
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", config);
//        return source;
//    }
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
            CorsConfiguration config = new CorsConfiguration();

            config.setAllowedOrigins(List.of(
                    "http://localhost:4200",
                    "http://192.168.1.4:4200", // ✅ Added IP-based frontend access
                    "http://192.168.0.210:4200",
                    "http://192.168.0.211:4200",
                    ""
            ));
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
            config.setAllowCredentials(true); // Enable if needed

            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", config);
            return source;
        }

}


