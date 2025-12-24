package com.perezper.authserver.service;

import com.perezper.authserver.entity.RefreshToken;
import com.perezper.authserver.entity.User;
import com.perezper.authserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final long refreshMs;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, RefreshTokenService refreshTokenService,
                       @Value("${jwt.refresh.expiration-ms}") long refreshMs) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.refreshMs = refreshMs;
    }

    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        return user;
    }

    public AuthTokens login(User user) {
        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);
        Instant expiry = Instant.now().plusMillis(refreshMs);
        refreshTokenService.createRefreshToken(user, refresh, expiry);
        return new AuthTokens(access, refresh, refreshMs);
    }

    public AuthTokens refresh(String refreshToken) {
        if (!jwtService.validateToken(refreshToken)) throw new RuntimeException("Invalid refresh token");
        String username = jwtService.getUsername(refreshToken);
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        // verify refresh token exists in DB
        RefreshToken rt = refreshTokenService.findByToken(refreshToken).orElseThrow(() -> new RuntimeException("Refresh token not recognized"));
        if (rt.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token expired");
        }
        // issue new
        String access = jwtService.generateAccessToken(user);
        String newRefresh = jwtService.generateRefreshToken(user);
        Instant expiry = Instant.now().plusMillis(refreshMs);
        refreshTokenService.deleteByUser(user);
        refreshTokenService.createRefreshToken(user, newRefresh, expiry);
        return new AuthTokens(access, newRefresh, refreshMs);
    }

    public void logout(String refreshToken) {
        refreshTokenService.findByToken(refreshToken).ifPresent(rt -> refreshTokenService.deleteByUser(rt.getUser()));
    }

    public static class AuthTokens {
        public final String accessToken;
        public final String refreshToken;
        public final long expiresIn;

        public AuthTokens(String accessToken, String refreshToken, long expiresIn) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.expiresIn = expiresIn;
        }
    }
}
