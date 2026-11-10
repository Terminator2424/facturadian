package com.facturadian.application.port.in;

import com.facturadian.domain.model.AppUser;
import java.util.Optional;

public interface UserUseCase {
    AppUser register(String username, String fullName, String email, String password, String confirmPassword);

    /** Devuelve el usuario si las credenciales son correctas; vacío en cualquier otro caso. */
    Optional<AppUser> authenticate(String username, String password);

    /** Crea al administrador (dueño de la tienda). Solo puede existir uno. */
    AppUser createAdmin(String username, String fullName, String email, String password);

    /** Para sesiones ya iniciadas: devuelve el usuario si existe y no está bloqueado. */
    Optional<AppUser> findActiveUser(String username);
}