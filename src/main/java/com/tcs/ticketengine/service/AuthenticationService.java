package com.tcs.ticketengine.service;

import com.tcs.ticketengine.entity.UserAccount;
import com.tcs.ticketengine.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.transaction.annotation.Transactional;
@Service
public class AuthenticationService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, String> sessions = new ConcurrentHashMap<>();

    public AuthenticationService(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResult login(String username, String password) {
        UserAccount user = userAccountRepository.findByUsername(normalizeUsername(username))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
        return createSession(user.getUsername());
    }

    @Transactional
    public LoginResult register(String username, String password) {
        String normalizedUsername = normalizeUsername(username);
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must contain at least 6 characters");
        }
        if (userAccountRepository.findByUsername(normalizedUsername).isPresent()) {
            throw new UserAlreadyExistsException("That username is already registered");
        }
        userAccountRepository.save(new UserAccount(normalizedUsername, passwordEncoder.encode(password)));
        return createSession(normalizedUsername);
    }

    public String requireUsername(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Login is required");
        }
        String username = sessions.get(authorizationHeader.substring(7));
        if (username == null) {
            throw new UnauthorizedException("Your session has expired. Please log in again");
        }
        return username;
    }

    private LoginResult createSession(String username) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, username);
        return new LoginResult(token, username);
    }

    private String normalizeUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new InvalidCredentialsException("Username is required");
        }
        return username.trim().toLowerCase();
    }

    public record LoginResult(String token, String username) {
    }

    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException(String message) {
            super(message);
        }
    }

    public static class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(String message) {
            super(message);
        }
    }

    public static class UserAlreadyExistsException extends RuntimeException {
        public UserAlreadyExistsException(String message) {
            super(message);
        }
    }
}
