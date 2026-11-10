package com.facturadian.infrastructure.adapter.in.rest.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/")
@Tag(name = "Inicio", description = "Página de entrada de la aplicación")
public class HomeController {

    @GET
    @Operation(summary = "Inicio", description = "Redirige al listado principal de facturas")
    public Response home() {
        return Response.seeOther(URI.create("/facturas")).build();
    }
}