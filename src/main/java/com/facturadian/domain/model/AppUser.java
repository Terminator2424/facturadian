package com.facturadian.domain.model;

import java.time.LocalDateTime;

/** Usuario de la aplicación. Incluye la regla de bloqueo por intentos fallidos. */
public class AppUser {
    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final int LOCK_MINUTES = 15;

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String passwordHash;
    private int failedAttempts;
    private LocalDateTime lockedUntil;
    private LocalDateTime createdAt = LocalDateTime.now();
    private Role role = Role.CASHIER;

    public boolean isLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    public void registerFailedAttempt() {
        if (lockedUntil != null && !isLocked()) {
            failedAttempts = 0;
            lockedUntil = null;
        }
        failedAttempts++;
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            lockedUntil = LocalDateTime.now().plusMinutes(LOCK_MINUTES);
        }
    }

    public void registerSuccessfulLogin() {
        failedAttempts = 0;
        lockedUntil = null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }
    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}