package com.facturadian.infrastructure.adapter.in.web;

import com.facturadian.domain.exception.BusinessException;
import com.facturadian.application.port.in.UserUseCase;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/")
@Tag(name = "Autenticación", description = "Vistas HTML de inicio de sesión, registro y cierre de sesión")
public class AuthController {

    @Inject
    UserUseCase users;

    @Inject
    @Location("auth/login")
    Template login;

    @Inject
    @Location("auth/register")
    Template register;

    @GET
    @Path("/login")
    @Produces(MediaType.TEXT_HTML)
    @Operation(summary = "Pantalla de inicio de sesión")
    public TemplateInstance loginPage(@QueryParam("error") String error,
                                      @QueryParam("registered") String registered) {
        return login.data("error", error != null).data("registered", registered != null);
    }

    @GET
    @Path("/registro")
    @Produces(MediaType.TEXT_HTML)
    @Operation(summary = "Pantalla de registro")
    public TemplateInstance registerPage() {
        return register.instance();
    }

    @POST
    @Path("/registro")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @Operation(summary = "Registrar usuario")
    public Response doRegister(@FormParam("username") String username,
                               @FormParam("fullName") String fullName,
                               @FormParam("email") String email,
                               @FormParam("password") String password,
                               @FormParam("confirm") String confirm) {
        try {
            users.register(username, fullName, email, password, confirm);
            return Response.seeOther(URI.create("/login?registered=true")).build();
        } catch (BusinessException e) {
            String html = register.data("error", e.getMessage())
                    .data("username", username)
                    .data("fullName", fullName)
                    .data("email", email)
                    .render();
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_HTML).entity(html).build();
        }
    }

    @POST
    @Path("/salir")
    @Operation(summary = "Cerrar sesión")
    public Response logout() {
        NewCookie expired = new NewCookie.Builder("facturadian-session")
                .value("").path("/").maxAge(0).httpOnly(true).build();
        return Response.seeOther(URI.create("/login")).cookie(expired).build();
    }
}