package com.perezper.authserver.controller;

import com.perezper.authserver.dto.*;
import com.perezper.authserver.entity.User;
import com.perezper.authserver.service.AuthService;
import com.perezper.authserver.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<StandardResponse<Object>> register(@RequestBody StandardRequest<RegisterRequest> req) {
        RegisterRequest r = req.getPayload();
        User u = userService.register(r.getUsername(), r.getPassword(), r.getEmail());
        Map<String, Object> out = new HashMap<>();
        out.put("id", u.getId());
        out.put("username", u.getUsername());
        return ResponseEntity.ok(new StandardResponse<>(true, out, null));
    }

    @PostMapping("/login")
    public ResponseEntity<StandardResponse<AuthResponse>> login(@RequestBody StandardRequest<AuthRequest> req) {
        AuthRequest r = req.getPayload();
        User u = authService.authenticate(r.getUsername(), r.getPassword());
        AuthService.AuthTokens tokens = authService.login(u);
        AuthResponse resp = new AuthResponse(tokens.accessToken, tokens.refreshToken, tokens.expiresIn);
        return ResponseEntity.ok(new StandardResponse<>(true, resp, null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<StandardResponse<AuthResponse>> refresh(@RequestBody StandardRequest<Map<String,String>> req) {
        String refresh = req.getPayload().get("refreshToken");
        AuthService.AuthTokens tokens = authService.refresh(refresh);
        AuthResponse resp = new AuthResponse(tokens.accessToken, tokens.refreshToken, tokens.expiresIn);
        return ResponseEntity.ok(new StandardResponse<>(true, resp, null));
    }

    @PostMapping("/logout")
    public ResponseEntity<StandardResponse<Object>> logout(@RequestBody StandardRequest<Map<String,String>> req) {
        String refresh = req.getPayload().get("refreshToken");
        authService.logout(refresh);
        return ResponseEntity.ok(new StandardResponse<>(true, null, null));
    }

    @GetMapping("/userinfo")
    public ResponseEntity<StandardResponse<Object>> userinfo(Authentication authentication) {
        if (authentication == null)
            return ResponseEntity.status(401).body(new StandardResponse<>(false, null, new ErrorResponse("Unauthorized")));
        String username = (String) authentication.getPrincipal();
        User user = userService.findByUsername(username);
        Map<String,Object> out = new HashMap<>();
        out.put("username", user.getUsername());
        out.put("email", user.getEmail());
        out.put("roles", user.getRoles().stream().map(r -> r.getName()).toArray());
        return ResponseEntity.ok(new StandardResponse<>(true, out, null));
    }
}
