package com.wfm.experts.websocket;

import com.wfm.experts.exception.JwtAuthenticationException;
import com.wfm.experts.notificationengine.producer.impl.NotificationProducerImpl;
import com.wfm.experts.security.JwtUtil;
import com.wfm.experts.tenancy.TenantContext;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthChannelInterceptor.class);

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public WebSocketAuthChannelInterceptor(@Lazy JwtUtil jwtUtil,
                                           @Lazy UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(@Nullable Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        StompCommand command = accessor.getCommand();
        if (StompCommand.CONNECT.equals(command)) {
            logger.info("STOMP CONNECT received");
            handleConnect(accessor);
        } else if (StompCommand.SEND.equals(command) || StompCommand.SUBSCRIBE.equals(command)) {
            handleTenantContextPropagation(accessor);
        }

        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        try {
            List<String> authorizationHeaders = accessor.getNativeHeader("Authorization");
            if (authorizationHeaders == null || authorizationHeaders.isEmpty()) {
                logger.warn("STOMP CONNECT :: Missing Authorization header");
                throw new JwtAuthenticationException("Authorization header is required in CONNECT frame");
            }

            String token = extractToken(authorizationHeaders.get(0));
            String email = jwtUtil.extractEmail(token);
            String tenantId = jwtUtil.extractTenantId(token);

            jwtUtil.validateToken(token, email);
            TenantContext.setTenant(tenantId);

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            if (userDetails == null) {
                throw new JwtAuthenticationException("User not found for email: " + email);
            }

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            accessor.setUser(authentication);

            if (accessor.getSessionAttributes() != null) {
                accessor.getSessionAttributes().put(NotificationProducerImpl.TENANT_ID_HEADER, tenantId);
            }

            logger.info("STOMP CONNECT :: Authenticated '{}' for tenant '{}'", email, tenantId);

        } catch (Exception e) {
            logger.error("STOMP CONNECT :: Authentication failed: {}", e.getMessage(), e);
            TenantContext.clear();
            throw new JwtAuthenticationException("WebSocket authentication failed", e);
        } finally {
            TenantContext.clear();
        }
    }

    private void handleTenantContextPropagation(StompHeaderAccessor accessor) {
        if (accessor.getUser() != null && accessor.getSessionAttributes() != null) {
            String tenantId = (String) accessor.getSessionAttributes().get(NotificationProducerImpl.TENANT_ID_HEADER);
            if (tenantId != null) {
                TenantContext.setTenant(tenantId);
                logger.debug("STOMP {} :: TenantContext set to '{}'", accessor.getCommand(), tenantId);
            } else {
                logger.warn("STOMP {} :: TenantId missing from session attributes", accessor.getCommand());
            }
        } else {
            logger.warn("STOMP {} :: No user or session attributes available", accessor.getCommand());
        }
    }

    private String extractToken(String headerValue) {
        if (StringUtils.hasText(headerValue) && headerValue.startsWith("Bearer ")) {
            return headerValue.substring(7).trim();
        }
        throw new JwtAuthenticationException("Authorization header must be in the format 'Bearer <token>'");
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && (
                StompCommand.SEND.equals(accessor.getCommand()) ||
                        StompCommand.SUBSCRIBE.equals(accessor.getCommand()) ||
                        StompCommand.DISCONNECT.equals(accessor.getCommand()))) {
            logger.debug("🧹 STOMP {} :: Clearing TenantContext for user: {}",
                    accessor.getCommand(), accessor.getUser() != null ? accessor.getUser().getName() : "anonymous");
            TenantContext.clear();
        }

        if (ex != null) {
            logger.error("❗ STOMP Error during {}: {}", accessor != null ? accessor.getCommand() : "unknown", ex.getMessage(), ex);
            TenantContext.clear();
        }
    }
}
