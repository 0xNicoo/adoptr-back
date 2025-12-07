package com.adoptr.adoptr.shared.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class MembershipSupport {

    public Long getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            throw new IllegalStateException("Authentication is null — no user logged in.");
        }

        return (Long) auth.getPrincipal();
    }

}
