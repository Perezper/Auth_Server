package com.perezper.authserver.controller;

import com.perezper.authserver.dto.*;
import com.perezper.authserver.entity.RefreshToken;
import com.perezper.authserver.entity.User;
import com.perezper.authserver.service.JwtService;
import com.perezper.authserver.service.RefreshTokenService;
import com.perezper.authserver.service.UserService;
import io.jsonwebtoken.Claims;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/oauth2")
public class OAuth2Controller {
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    public OAuth2Controller(JwtService jwtService, RefreshTokenService refreshTokenService, UserService userService) {
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.userService = userService;
    }

    @PostMapping("/introspect")
    public ResponseEntity<StandardResponse<IntrospectResponse>> introspect(@RequestBody StandardRequest<IntrospectRequest> req) {
        String token = req.getPayload() != null ? req.getPayload().getToken() : null;
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(new StandardResponse<>(false, null, new ErrorResponse("token required")));
        }

        boolean valid = jwtService.validateToken(token);
        boolean expired = jwtService.isTokenExpired(token);
        // check if refresh token exists in DB
        RefreshToken stored = refreshTokenService.findByToken(token).orElse(null);
        boolean isRefresh = stored != null;
        boolean revoked = false;
        String username = null;
        String[] scopes = new String[0];
        Instant expInstant = null;

        if (valid) {
            try {
                Claims claims = jwtService.getClaims(token);
                username = claims.getSubject();
                if (claims.getExpiration() != null) expInstant = claims.getExpiration().toInstant();
                scopes = jwtService.getRolesFromToken(token);
            } catch (Exception ignored) {}
        }

        if (isRefresh) {
            // if token record is missing OR expiry passed -> consider revoked/expired
            if (stored == null) revoked = true;
            else if (stored.getExpiryDate() != null && stored.getExpiryDate().isBefore(Instant.now())) expired = true;
        }

        IntrospectResponse out = new IntrospectResponse();
        out.setActive(valid && !expired && !revoked);
        out.setTokenType(isRefresh ? "refresh" : "access");
        out.setExpired(expired);
        out.setRevoked(revoked);
        out.setUsername(username);
        out.setScopes(scopes);
        out.setExp(expInstant);

        return ResponseEntity.ok(new StandardResponse<>(true, out, null));
    }
}
