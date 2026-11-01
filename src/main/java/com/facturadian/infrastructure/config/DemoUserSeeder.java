package com.facturadian.infrastructure.config;

import com.facturadian.domain.exception.BusinessException;
import com.facturadian.domain.port.input.UserUseCase;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/** Crea un usuario de demostración al arrancar, solo si la propiedad app.seed-demo-user es true. */
@ApplicationScoped
public class DemoUserSeeder {

    @Inject
    UserUseCase users;

    @ConfigProperty(name = "app.seed-demo-user", defaultValue = "false")
    boolean enabled;

    void onStart(@Observes StartupEvent event) {
        if (!enabled || users.findActiveUser("demo").isPresent()) {
            return;
        }
        try {
            users.register("demo", "Usuario Demostración", "demo@facturadian.com",
                    "Fact2026*Dian", "Fact2026*Dian");
        } catch (BusinessException e) {
            // Ya existe: no se hace nada.
        }
    }
}