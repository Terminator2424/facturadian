package com.facturadian.infrastructure.adapter.in.rest.resource;

import static com.facturadian.infrastructure.adapter.in.rest.resource.ViewSupport.parseDate;
import static com.facturadian.infrastructure.adapter.in.rest.resource.ViewSupport.redirect;
import static com.facturadian.infrastructure.adapter.in.rest.resource.ViewSupport.withMessages;

import com.facturadian.domain.exception.BusinessException;
import com.facturadian.domain.model.SalesSummary;
import com.facturadian.application.port.in.InvoiceUseCase;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/reportes")
@Tag(name = "Reportes", description = "Resumen de ventas por rango de fechas y su PDF")
public class ReportController {

    @Inject
    InvoiceUseCase invoiceUseCase;

    @Inject
    @Location("report/summary")
    Template summaryTemplate;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Operation(summary = "Resumen de ventas", description = "Totales de facturación entre dos fechas (por defecto, el mes actual)")
    public TemplateInstance summary(@QueryParam("from") String from,
                                    @QueryParam("to") String to,
                                    @QueryParam("success") String success,
                                    @QueryParam("error") String error) {
        String message = error;
        SalesSummary summary;
        try {
            LocalDate fromDate = parseDate(from);
            LocalDate toDate = parseDate(to);
            summary = invoiceUseCase.getSalesSummary(fromDate, toDate);
        } catch (BusinessException e) {
            message = e.getMessage();
            summary = invoiceUseCase.getSalesSummary(null, null);
        }
        return withMessages(summaryTemplate.data("summary", summary), success, message);
    }

    @GET
    @Path("/pdf")
    @Produces("application/pdf")
    @Operation(summary = "Descargar resumen en PDF", description = "Genera con OpenPDF el resumen de ventas del rango indicado")
    public Response downloadPdf(@QueryParam("from") String from,
                                @QueryParam("to") String to) {
        try {
            byte[] pdf = invoiceUseCase.generateSummaryPdf(parseDate(from), parseDate(to));
            return Response.ok(pdf)
                    .header("Content-Disposition", "attachment; filename=\"resumen_ventas.pdf\"")
                    .build();
        } catch (BusinessException e) {
            return redirect("/reportes", null, e.getMessage());
        }
    }
}