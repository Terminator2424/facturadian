package com.facturadian.infrastructure.adapter.out.security;

import com.facturadian.domain.port.output.PasswordHasherPort;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;

/** Adaptador de salida: cifra y compara contraseñas con BCrypt. */
@ApplicationScoped
public class BcryptPasswordHasherAdapter implements PasswordHasherPort {

    @Override
    public String hash(String rawPassword) {
        return BcryptUtil.bcryptHash(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String hash) {
        return BcryptUtil.matches(rawPassword, hash);
    }
}