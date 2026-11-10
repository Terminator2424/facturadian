package com.facturadian.infrastructure.adapter.in.rest.resource;

import static com.facturadian.infrastructure.adapter.in.rest.resource.ViewSupport.isBlank;
import static com.facturadian.infrastructure.adapter.in.rest.resource.ViewSupport.redirect;
import static com.facturadian.infrastructure.adapter.in.rest.resource.ViewSupport.withMessages;

import com.facturadian.application.port.in.ClientUseCase;
import com.facturadian.application.port.in.InvoiceUseCase;
import com.facturadian.application.port.in.InvoiceUseCase.ItemRequest;
import com.facturadian.application.port.in.ProductUseCase;
import com.facturadian.domain.exception.BusinessException;
import com.facturadian.domain.model.Invoice;
import com.facturadian.domain.model.InvoiceStatus;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/** Adaptador de entrada: recibe las peticiones del navegador y llama a los casos de uso. */
@Path("/facturas")
@Tag(name = "Facturas", description = "Vistas HTML para crear, consultar y cambiar el estado de las facturas")
public class InvoiceController {

    @Inject
    InvoiceUseCase invoiceUseCase;

    @Inject
    ClientUseCase clientUseCase;

    @Inject
    ProductUseCase productUseCase;

    @Inject
    SecurityIdentity identity;

    @Inject
    @Location("invoice/list")
    Template listTemplate;

    @Inject
    @Location("invoice/form")
    Template formTemplate;

    @Inject
    @Location("invoice/detail")
    Template detailTemplate;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Operation(summary = "Listado de facturas", description = "Página principal. Permite filtrar por estado (ISSUED, PAID, CANCELLED)")
    public TemplateInstance list(@QueryParam("estado") String estado,
                                 @QueryParam("success") String success,
                                 @QueryParam("error") String error) {
        InvoiceStatus status = parseStatus(estado);
        TemplateInstance template = listTemplate
                .data("invoices", invoiceUseCase.listInvoices(status))
                .data("statuses", List.of(InvoiceStatus.values()))
                .data("selected", status == null ? "" : status.name());
        return withMessages(template, success, error);
    }

    @GET
    @Path("/nueva")
    @RolesAllowed("cashier")
    @Produces(MediaType.TEXT_HTML)
    @Operation(summary = "Formulario de nueva factura", description = "Muestra el formulario: se elige el cliente y se escanean o buscan los productos")
    public TemplateInstance newInvoice(@QueryParam("success") String success,
                                       @QueryParam("error") String error) {
        TemplateInstance template = formTemplate.data("clients", clientUseCase.listClients());
        return withMessages(template, success, error);
    }

    @POST
    @Path("/crear")
    @RolesAllowed("cashier")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(summary = "Crear factura", description = "Crea la factura con los productos del catálogo, calcula IVA y ReteIVA y redirige al detalle")
    public Response create(@FormParam("clientId") Long clientId,
                           @FormParam("productId") List<Long> productIds,
                           @FormParam("quantity") List<String> quantities) {
        try {
            List<ItemRequest> items = new ArrayList<>();
            for (int i = 0; i < productIds.size(); i++) {
                String quantity = i < quantities.size() ? quantities.get(i) : null;
                items.add(buildLine(productIds.get(i), quantity, i + 1));
            }
            Invoice invoice = invoiceUseCase.createInvoice(clientId, items, identity.getPrincipal().getName());
            return redirect("/facturas/" + invoice.getId(),
                    "Factura " + invoice.getInvoiceNumber() + " creada correctamente.", null);
        } catch (BusinessException e) {
            return redirect("/facturas/nueva", null, e.getMessage());
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    @Operation(summary = "Detalle de factura", description = "Muestra ítems, totales y acciones (agregar ítem, pagar, anular, PDF)")
    public TemplateInstance detail(@PathParam("id") Long id,
                                   @QueryParam("success") String success,
                                   @QueryParam("error") String error) {
        try {
            Invoice invoice = invoiceUseCase.getInvoice(id);
            TemplateInstance template = detailTemplate
                    .data("invoice", invoice)
                    .data("editable", invoice.getStatus() == InvoiceStatus.ISSUED)
                    .data("products", productUseCase.listActiveProducts());
            return withMessages(template, success, error);
        } catch (BusinessException e) {
            throw new WebApplicationException(redirect("/facturas", null, e.getMessage()));
        }
    }

    @POST
    @Path("/{id}/items")
    @RolesAllowed("cashier")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(summary = "Agregar ítem", description = "Agrega un producto del catálogo a una factura Emitida y recalcula los totales")
    public Response addItem(@PathParam("id") Long id,
                            @FormParam("productId") Long productId,
                            @FormParam("quantity") String quantity) {
        try {
            invoiceUseCase.addItem(id, buildLine(productId, quantity, 0));
            return redirect("/facturas/" + id, "Ítem agregado correctamente.", null);
        } catch (BusinessException e) {
            return redirect("/facturas/" + id, null, e.getMessage());
        }
    }

    @POST
    @Path("/{id}/pagar")
    @Operation(summary = "Marcar como pagada", description = "Cambia el estado de Emitida a Pagada")
    public Response pay(@PathParam("id") Long id) {
        try {
            invoiceUseCase.markAsPaid(id);
            return redirect("/facturas/" + id, "La factura fue marcada como pagada.", null);
        } catch (BusinessException e) {
            return redirect("/facturas/" + id, null, e.getMessage());
        }
    }

    @POST
    @Path("/{id}/anular")
    @Operation(summary = "Anular factura", description = "Cambia el estado a Anulada (no aplica a facturas pagadas)")
    public Response cancel(@PathParam("id") Long id) {
        try {
            invoiceUseCase.cancelInvoice(id);
            return redirect("/facturas/" + id, "La factura fue anulada.", null);
        } catch (BusinessException e) {
            return redirect("/facturas/" + id, null, e.getMessage());
        }
    }

    @GET
    @Path("/{id}/pdf")
    @Produces("application/pdf")
    @Operation(summary = "Descargar comprobante PDF", description = "Genera con OpenPDF el comprobante de la factura")
    public Response downloadPdf(@PathParam("id") Long id) {
        try {
            byte[] pdf = invoiceUseCase.generateInvoicePdf(id);
            return Response.ok(pdf)
                    .header("Content-Disposition", "attachment; filename=\"factura_" + id + ".pdf\"")
                    .build();
        } catch (BusinessException e) {
            return redirect("/facturas", null, e.getMessage());
        }
    }

    private InvoiceStatus parseStatus(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return InvoiceStatus.valueOf(value.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Solo se lee el producto y la cantidad: el nombre y el precio los pone el catálogo. */
    private ItemRequest buildLine(Long productId, String quantity, int row) {
        String where = row > 0 ? " (línea " + row + ")" : "";
        int qty;
        try {
            qty = Integer.parseInt(quantity == null ? "" : quantity.trim());
        } catch (NumberFormatException e) {
            throw new BusinessException("La cantidad no es un número entero válido" + where + ".");
        }
        return new ItemRequest(productId, qty);
    }
}