package com.timz.rag_platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class SecurityAuditListener {

    private static final Logger logger = LoggerFactory.getLogger(SecurityAuditListener.class);

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        logger.info("[SECURITE] Connexion reussie : {}", username);
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        String username = event.getAuthentication().getName();
        String reason = event.getException().getMessage();
        logger.warn("[SECURITE] Echec de connexion : {} - Raison : {}", username, reason);
    }
}