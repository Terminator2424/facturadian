package com.facturadian.infrastructure.adapter.out.persistence.mapper;

import com.facturadian.domain.model.AppUser;
import com.facturadian.domain.model.Role;
import com.facturadian.infrastructure.adapter.out.persistence.entity.UserEntity;

public final class UserMapper {
    private UserMapper() {}

    public static AppUser toDomain(UserEntity e) {
        if (e == null) return null;
        AppUser u = new AppUser();
        u.setId(e.getId());
        u.setUsername(e.getUsername());
        u.setFullName(e.getFullName());
        u.setEmail(e.getEmail());
        u.setPasswordHash(e.getPasswordHash());
        u.setFailedAttempts(e.getFailedAttempts());
        u.setLockedUntil(e.getLockedUntil());
        u.setCreatedAt(e.getCreatedAt());
        u.setRole(e.getRole() == null ? Role.CASHIER : Role.valueOf(e.getRole()));
        return u;
    }

    public static UserEntity toEntity(AppUser d) {
        if (d == null) return null;
        UserEntity e = new UserEntity();
        e.setId(d.getId());
        e.setUsername(d.getUsername());
        e.setFullName(d.getFullName());
        e.setEmail(d.getEmail());
        e.setPasswordHash(d.getPasswordHash());
        e.setFailedAttempts(d.getFailedAttempts());
        e.setLockedUntil(d.getLockedUntil());
        e.setCreatedAt(d.getCreatedAt());
        e.setRole(d.getRole().name());
        return e;
    }
}