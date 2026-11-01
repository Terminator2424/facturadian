package com.facturadian.infrastructure.adapter.in.security;

import com.facturadian.domain.model.AppUser;
import com.facturadian.domain.port.input.UserUseCase;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.UsernamePasswordAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;

/** Conecta el login de Quarkus con el caso de uso de usuarios del dominio. */
@ApplicationScoped
public class UserIdentityProvider implements IdentityProvider<UsernamePasswordAuthenticationRequest> {

    @Inject
    UserUseCase users;

    @Override
    public Class<UsernamePasswordAuthenticationRequest> getRequestType() {
        return UsernamePasswordAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(UsernamePasswordAuthenticationRequest request,
                                              AuthenticationRequestContext context) {
        return context.runBlocking(() -> {
            String password = new String(request.getPassword().getPassword());
            Optional<AppUser> user = users.authenticate(request.getUsername(), password);
            if (user.isEmpty()) {
                throw new AuthenticationFailedException();
            }
            return QuarkusSecurityIdentity.builder()
                    .setPrincipal(new QuarkusPrincipal(user.get().getUsername()))
                    .addRole("user")
                    .addAttribute("fullName", user.get().getFullName())
                    .build();
        });
    }
}