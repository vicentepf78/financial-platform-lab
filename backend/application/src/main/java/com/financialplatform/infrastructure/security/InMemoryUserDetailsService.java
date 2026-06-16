package com.financialplatform.infrastructure.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class InMemoryUserDetailsService implements UserDetailsService {

    private final Map<String, UserDetails> users;

    public InMemoryUserDetailsService(JwtProperties jwtProperties) {
        Objects.requireNonNull(jwtProperties, "JwtProperties is required");
        this.users = buildUsers(jwtProperties);
    }

    private static Map<String, UserDetails> buildUsers(JwtProperties jwtProperties) {
        Map<String, UserDetails> built = new HashMap<>();
        jwtProperties.getUsers().forEach((username, userProperties) -> {
            String[] roles = userProperties.getRoles().stream()
                    .flatMap(role -> Arrays.stream(role.split(",")))
                    .map(String::trim)
                    .filter(role -> !role.isEmpty())
                    .toArray(String[]::new);
            built.put(
                    username,
                    User.withUsername(username)
                            .password("{noop}" + userProperties.getPassword())
                            .roles(roles)
                            .build());
        });
        return Map.copyOf(built);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails user = users.get(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return user;
    }
}
