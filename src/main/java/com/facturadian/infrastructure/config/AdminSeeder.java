package com.facturadian.infrastructure.config;

import com.facturadian.application.port.in.UserUseCase;
import com.facturadian.domain.exception.BusinessException;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/** Crea al administrador único (dueño de la tienda) al arrancar. Su clave sale de ADMIN_PASSWORD. */
@ApplicationScoped
public class AdminSeeder {

    @Inject
    UserUseCase users;

    @ConfigProperty(name = "app.admin.username", defaultValue = "admin")
    String username;

    @ConfigProperty(name = "app.admin.password")
    Optional<String> password;

    void onStart(@Observes StartupEvent event) {
        if (password.isEmpty() || password.get().isBlank()) {
            Log.warn("No hay clave de administrador: define ADMIN_PASSWORD en el .env para crear al dueño de la tienda.");
            return;
        }
        try {
            users.createAdmin(username, "Dueño de la tienda", "admin@facturadian.com", password.get());
            Log.info("Administrador creado: " + username);
        } catch (BusinessException e) {
            Log.info("Administrador no creado: " + e.getMessage());
        }
    }
}