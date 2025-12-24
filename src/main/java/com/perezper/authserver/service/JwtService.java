package com.perezper.authserver.service;

import com.perezper.authserver.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.Arrays;

@Service
public class JwtService {
    private final SecretKey secretKey;
    private final long accessMs;
    private final long refreshMs;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.access.expiration-ms}") long accessMs,
                      @Value("${jwt.refresh.expiration-ms}") long refreshMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessMs = accessMs;
        this.refreshMs = refreshMs;
    }

    public String generateAccessToken(User user) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessMs);
        String roles = user.getRoles().stream().map(r -> r.getName()).collect(Collectors.joining(","));
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + refreshMs);
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public String getUsername(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaims(token);
            Date exp = claims.getExpiration();
            return exp == null || exp.before(new Date());
        } catch (Exception ex) {
            return true;
        }
    }

    public String[] getRolesFromToken(String token) {
        try {
            Claims claims = getClaims(token);
            Object rolesObj = claims.get("roles");
            if (rolesObj == null) return new String[0];
            String rolesStr = rolesObj.toString();
            return Arrays.stream(rolesStr.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);
        } catch (Exception ex) {
            return new String[0];
        }
    }
}
