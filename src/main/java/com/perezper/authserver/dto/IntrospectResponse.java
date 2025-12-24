package com.perezper.authserver.dto;

import java.time.Instant;

public class IntrospectResponse {
    private boolean active;
    private String tokenType; // access | refresh
    private boolean expired;
    private boolean revoked;
    private String username;
    private String[] scopes;
    private Instant exp;

    public IntrospectResponse() {}

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public boolean isExpired() { return expired; }
    public void setExpired(boolean expired) { this.expired = expired; }
    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String[] getScopes() { return scopes; }
    public void setScopes(String[] scopes) { this.scopes = scopes; }
    public Instant getExp() { return exp; }
    public void setExp(Instant exp) { this.exp = exp; }
}
