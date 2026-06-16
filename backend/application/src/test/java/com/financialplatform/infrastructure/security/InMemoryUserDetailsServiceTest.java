package com.financialplatform.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryUserDetailsServiceTest {

    private InMemoryUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new InMemoryUserDetailsService();
    }

    @Test
    void shouldLoadOperatorWithOperatorRole() {
        UserDetails user = userDetailsService.loadUserByUsername("operator");

        assertThat(user.getUsername()).isEqualTo("operator");
        assertThat(user.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_OPERATOR");
    }

    @Test
    void shouldLoadAdminWithAdminAndOperatorRoles() {
        UserDetails user = userDetailsService.loadUserByUsername("admin");

        assertThat(user.getUsername()).isEqualTo("admin");
        assertThat(user.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_OPERATOR");
    }

    @Test
    void shouldRejectUnknownUser() {
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
