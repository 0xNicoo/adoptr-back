package com.adoptr.adoptr.shared.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class MembershipSupportTest {

    private final MembershipSupport membershipSupport = new MembershipSupport();

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void findUserIdVacioSinAutenticacion() {
        assertThat(membershipSupport.findUserId()).isEmpty();
    }

    @Test
    void findUserIdDevuelveElPrincipalLong() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(7L, null, Collections.emptyList()));

        assertThat(membershipSupport.findUserId()).contains(7L);
    }

    @Test
    void findUserIdVacioSiElPrincipalNoEsUnId() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonymousUser", null, Collections.emptyList()));

        assertThat(membershipSupport.findUserId()).isEmpty();
    }
}
