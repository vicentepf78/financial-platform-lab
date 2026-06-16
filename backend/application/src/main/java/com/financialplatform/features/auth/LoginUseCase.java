package com.financialplatform.features.auth;

import com.financialplatform.infrastructure.security.JwtProperties;
import com.financialplatform.infrastructure.security.JwtService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Objects;

public class LoginUseCase {

    private static final String TOKEN_TYPE = "Bearer";

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public LoginUseCase(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            JwtProperties jwtProperties) {
        this.userDetailsService = Objects.requireNonNull(userDetailsService, "UserDetailsService is required");
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "PasswordEncoder is required");
        this.jwtService = Objects.requireNonNull(jwtService, "JwtService is required");
        this.jwtProperties = Objects.requireNonNull(jwtProperties, "JwtProperties is required");
    }

    public LoginResult execute(LoginCommand command) {
        Objects.requireNonNull(command, "Command is required");

        UserDetails user = loadUser(command.username());

        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user.getUsername(), extractRoles(user));
        return new LoginResult(token, TOKEN_TYPE, jwtProperties.getExpirationSeconds());
    }

    private UserDetails loadUser(String username) {
        try {
            return userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException ex) {
            throw new InvalidCredentialsException();
        }
    }

    private static List<String> extractRoles(UserDetails user) {
        return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(LoginUseCase::stripRolePrefix)
                .toList();
    }

    private static String stripRolePrefix(String authority) {
        return authority.startsWith("ROLE_") ? authority.substring("ROLE_".length()) : authority;
    }
}
