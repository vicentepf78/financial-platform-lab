package com.financialplatform.account.infrastructure;

import com.financialplatform.account.ports.AuthenticatedActorPort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SystemAuthenticatedActorAdapter implements AuthenticatedActorPort {

    @Override
    public String currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null && !authentication.getName().isBlank()) {
            return authentication.getName();
        }
        return "system";
    }
}
