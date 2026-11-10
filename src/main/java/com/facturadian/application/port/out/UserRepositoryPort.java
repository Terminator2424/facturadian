package com.facturadian.application.port.out;

import com.facturadian.domain.model.AppUser;
import java.util.Optional;

public interface UserRepositoryPort {
    AppUser save(AppUser user);
    Optional<AppUser> findByUsername(String username);
    Optional<AppUser> findByEmail(String email);
    boolean existsAdmin();
}