package com.wfm.experts.config;

import com.wfm.experts.notificationengine.producer.impl.NotificationProducerImpl;
import com.wfm.experts.security.JwtUtil;
import com.wfm.experts.tenancy.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService; // Spring Security's UserDetailsService
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;
import java.util.Objects;

@Configuration
@EnableWebSocketMessageBroker // Enables WebSocket message handling, backed by a message broker.
@Order(Ordered.HIGHEST_PRECEDENCE + 99) // Ensure Spring Security processes HTTP requests first
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    @Autowired
    private JwtUtil jwtUtil;

    // Use @Lazy to avoid circular dependency issues if UserDetailsService also depends on WebSocket components
    @Autowired
    @Lazy
    private UserDetailsService userDetailsService; // This is com.wfm.experts.service.EmployeeService

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Destination prefixes for messages bound for @MessageMapping-annotated methods in controller beans.
        config.setApplicationDestinationPrefixes("/app");

        // Enable a simple in-memory message broker to carry messages back to the client on destinations prefixed with "/topic" or "/queue".
        // For user-specific messages, Spring uses "/user" prefix.
        config.enableSimpleBroker("/topic", "/queue");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Registers the "/ws" endpoint, enabling SockJS fallback options so that alternate transports
        // can be used if WebSocket is not available.
        // SockJS is used to enable cross-origin requests.
        registry.addEndpoint("/ws") // This is the HTTP URL for the WebSocket handshake
                .setAllowedOriginPatterns("*") // Configure this properly for production (e.g., "http://localhost:4200")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    List<String> authorizationHeaders = accessor.getNativeHeader("Authorization");
                    String jwtToken = null;

                    if (authorizationHeaders != null && !authorizationHeaders.isEmpty()) {
                        String authHeaderVal = authorizationHeaders.get(0);
                        if (StringUtils.hasText(authHeaderVal) && authHeaderVal.startsWith("Bearer ")) {
                            jwtToken = authHeaderVal.substring(7);
                        }
                    }

                    if (jwtToken != null) {
                        try {
                            String email = jwtUtil.extractEmail(jwtToken);
                            String tenantId = jwtUtil.extractTenantId(jwtToken);
                            jwtUtil.validateToken(jwtToken, email);

                            // Set TenantContext for userDetailsService.loadUserByUsername to work correctly
                            TenantContext.setTenant(tenantId);
                            logger.debug("WebSocket CONNECT: TenantContext set to '{}' for user {}", tenantId, email);

                            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                            // It's crucial that userDetails.getUsername() returns the identifier
                            // that clients will use in their user-specific subscriptions.
                            // E.g., if client subscribes to /user/{employeeId}/queue/...,
                            // then userDetails.getUsername() should be that employeeId.
                            // If it's email, then client subscribes to /user/{email}/queue/...
                            // For this example, we assume userDetails.getUsername() IS the email,
                            // and we'll use that for the STOMP Principal.
                            // If you need to use another ID (like a numerical userId or employeeId)
                            // for user destinations, you'll need to ensure that ID is part of the Principal
                            // or stored in session attributes.
                            // For simplicity, let's assume the Principal name will be the email.
                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                            accessor.setUser(authentication); // Associates the user with the WebSocket session

                            // Store tenantId and actual userId (if different from principal name) in session attributes
                            if (accessor.getSessionAttributes() != null) {
                                accessor.getSessionAttributes().put(NotificationProducerImpl.TENANT_ID_HEADER, tenantId);
                                // If your Employee entity (via CustomUserDetails) has a specific userId field:
                                // com.wfm.experts.security.CustomUserDetails customUserDetails = (com.wfm.experts.security.CustomUserDetails) userDetails;
                                // accessor.getSessionAttributes().put("userId", customUserDetails.getEmployee().getEmployeeId()); // Or getEmployeeId()
                                logger.info("User '{}' on tenant '{}' authenticated for WebSocket session.", email, tenantId);
                            }

                        } catch (AuthenticationException e) {
                            logger.error("WebSocket authentication failed during CONNECT: {}", e.getMessage());
                            return null; // Deny connection by returning null
                        } catch (Exception e) {
                            logger.error("Error during WebSocket CONNECT frame processing: {}", e.getMessage(), e);
                            return null; // Deny connection
                        } finally {
                            // Clear TenantContext after this specific CONNECT processing
                            TenantContext.clear();
                            logger.debug("WebSocket CONNECT: TenantContext cleared for user after auth attempt.");
                        }
                    } else {
                        logger.warn("No JWT token found in CONNECT frame's Authorization header. Connection will be anonymous or rejected.");
                        // Depending on your policy, you might explicitly deny the connection here.
                        // For now, if no token, user remains unauthenticated for STOMP.
                    }
                }
                // For subsequent messages (SEND, SUBSCRIBE) from an authenticated client:
                // Set TenantContext if available from session, so @MessageMapping methods can use it.
                else if (accessor != null && (StompCommand.SEND.equals(accessor.getCommand()) || StompCommand.SUBSCRIBE.equals(accessor.getCommand()))) {
                    if (accessor.getUser() != null && accessor.getSessionAttributes() != null) {
                        String sessionTenantId = (String) accessor.getSessionAttributes().get(NotificationProducerImpl.TENANT_ID_HEADER);
                        if (sessionTenantId != null) {
                            TenantContext.setTenant(sessionTenantId);
                            logger.debug("TenantContext set to '{}' for STOMP {} from user: {}",
                                    sessionTenantId, accessor.getCommand(), accessor.getUser().getName());
                        } else {
                            logger.warn("TenantId not found in WebSocket session for STOMP {} by user: {}. TenantContext not set.",
                                    accessor.getCommand(), accessor.getUser().getName());
                        }
                    }
                }
                return message;
            }

            @Override
            public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
                // Clear TenantContext after processing an inbound message (SEND, SUBSCRIBE)
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && (StompCommand.SEND.equals(accessor.getCommand()) || StompCommand.SUBSCRIBE.equals(accessor.getCommand()))) {
                    if (TenantContext.getTenant() != null) { // Check if it was set
                        logger.debug("Clearing TenantContext after STOMP {} completion for user: {}",
                                accessor.getCommand(), (accessor.getUser() != null ? accessor.getUser().getName() : "unknown"));
                        TenantContext.clear();
                    }
                }
            }
        });
    }
}