package com.financialplatform.infrastructure.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class InMemoryUserDetailsService implements UserDetailsService {

    private static final Map<String, UserDetails> USERS = Map.of(
            "operator",
            User.withUsername("operator")
                    .password("{noop}operator")
                    .roles("OPERATOR")
                    .build(),
            "admin",
            User.withUsername("admin")
                    .password("{noop}admin")
                    .roles("ADMIN", "OPERATOR")
                    .build());

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails user = USERS.get(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return user;
    }
}
