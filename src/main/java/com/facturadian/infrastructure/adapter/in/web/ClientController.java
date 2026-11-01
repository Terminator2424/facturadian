package com.facturadian.infrastructure.adapter.in.web;

import static com.facturadian.infrastructure.adapter.in.web.ViewSupport.redirect;
import static com.facturadian.infrastructure.adapter.in.web.ViewSupport.withMessages;

import com.facturadian.domain.exception.BusinessException;
import com.facturadian.domain.model.Client;
import com.facturadian.domain.port.input.ClientUseCase;
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
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/clientes")
@Tag(name = "Clientes", description = "Vistas HTML para registrar y consultar clientes")
public class ClientController {

    @Inject
    ClientUseCase clientUseCase;

    @Inject
    @Location("client/list")
    Template listTemplate;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Operation(summary = "Listado de clientes", description = "Muestra los clientes y el formulario para registrar uno nuevo")
    public TemplateInstance list(@QueryParam("success") String success,
                                 @QueryParam("error") String error) {
        TemplateInstance template = listTemplate.data("clients", clientUseCase.listClients());
        return withMessages(template, success, error);
    }

    @POST
    @Path("/crear")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(summary = "Registrar cliente", description = "Valida el NIT (único) y el nombre, y guarda el cliente")
    public Response create(@FormParam("nit") String nit,
                           @FormParam("name") String name,
                           @FormParam("email") String email,
                           @FormParam("withholdingAgent") String withholdingAgent) {
        try {
            Client client = new Client(nit, name, email, withholdingAgent != null);
            Client saved = clientUseCase.createClient(client);
            return redirect("/clientes", "Cliente \"" + saved.getName() + "\" registrado correctamente.", null);
        } catch (BusinessException e) {
            return redirect("/clientes", null, e.getMessage());
        }
    }
}