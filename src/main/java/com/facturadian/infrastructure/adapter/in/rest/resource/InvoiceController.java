package com.facturadian.infrastructure.adapter.in.rest.resource;

import static com.facturadian.infrastructure.adapter.in.rest.resource.ViewSupport.isBlank;
import static com.facturadian.infrastructure.adapter.in.rest.resource.ViewSupport.redirect;
import static com.facturadian.infrastructure.adapter.in.rest.resource.ViewSupport.withMessages;

import com.facturadian.domain.exception.BusinessException;
import com.facturadian.domain.model.Invoice;
import com.facturadian.domain.model.InvoiceItem;
import com.facturadian.domain.model.InvoiceStatus;
import com.facturadian.application.port.in.ClientUseCase;
import com.facturadian.application.port.in.InvoiceUseCase;
import io.quarkus.qute.Location;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
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
import java.math.BigDecimal;
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
    @Produces(MediaType.TEXT_HTML)
    @Operation(summary = "Formulario de nueva factura", description = "Muestra el formulario con los clientes y 5 filas de ítems")
    public TemplateInstance newInvoice(@QueryParam("success") String success,
                                       @QueryParam("error") String error) {
        TemplateInstance template = formTemplate
                .data("clients", clientUseCase.listClients())
                .data("rows", List.of(1, 2, 3, 4, 5));
        return withMessages(template, success, error);
    }

    @POST
    @Path("/crear")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(summary = "Crear factura", description = "Crea la factura con sus ítems, calcula IVA y ReteIVA y redirige al detalle")
    public Response create(@FormParam("clientId") Long clientId,
                           @FormParam("productName") List<String> names,
                           @FormParam("unitPrice") List<String> prices,
                           @FormParam("quantity") List<String> quantities) {
        try {
            List<InvoiceItem> items = new ArrayList<>();
            for (int i = 0; i < names.size(); i++) {
                String name = names.get(i);
                String price = i < prices.size() ? prices.get(i) : null;
                String quantity = i < quantities.size() ? quantities.get(i) : null;
                // Las filas vacías (sin producto ni precio) se ignoran
                if (isBlank(name) && isBlank(price)) {
                    continue;
                }
                items.add(buildItem(name, price, quantity, i + 1));
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
                    .data("editable", invoice.getStatus() == InvoiceStatus.ISSUED);
            return withMessages(template, success, error);
        } catch (BusinessException e) {
            throw new WebApplicationException(redirect("/facturas", null, e.getMessage()));
        }
    }

    @POST
    @Path("/{id}/items")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(summary = "Agregar ítem", description = "Agrega un ítem a una factura Emitida y recalcula los totales")
    public Response addItem(@PathParam("id") Long id,
                            @FormParam("productName") String name,
                            @FormParam("unitPrice") String price,
                            @FormParam("quantity") String quantity) {
        try {
            invoiceUseCase.addItem(id, buildItem(name, price, quantity, 0));
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

    private InvoiceItem buildItem(String name, String price, String quantity, int row) {
        String where = row > 0 ? " (fila " + row + ")" : "";
        BigDecimal unitPrice;
        int qty;
        try {
            unitPrice = new BigDecimal(price == null ? "" : price.trim());
        } catch (NumberFormatException e) {
            throw new BusinessException("El precio no es un número válido" + where + ".");
        }
        try {
            qty = Integer.parseInt(quantity == null ? "" : quantity.trim());
        } catch (NumberFormatException e) {
            throw new BusinessException("La cantidad no es un número entero válido" + where + ".");
        }
        return new InvoiceItem(name == null ? null : name.trim(), unitPrice, qty);
    }
}