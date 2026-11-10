package com.facturadian.infrastructure.adapter.in.rest.resource;

import static com.facturadian.infrastructure.adapter.in.rest.resource.ViewSupport.redirect;
import static com.facturadian.infrastructure.adapter.in.rest.resource.ViewSupport.withMessages;

import com.facturadian.application.port.in.ProductUseCase;
import com.facturadian.domain.exception.BusinessException;
import com.facturadian.domain.model.Product;
import com.facturadian.infrastructure.adapter.in.rest.dto.ProductDto;
import com.facturadian.infrastructure.adapter.in.rest.mapper.ProductDtoMapper;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/productos")
@Tag(name = "Productos", description = "Catálogo de productos y búsqueda para el cajero")
public class ProductController {

    @Inject
    ProductUseCase productUseCase;

    @Inject
    @Location("product/list")
    Template listTemplate;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Operation(summary = "Catálogo de productos", description = "Lista los productos y el formulario para registrar uno nuevo")
    public TemplateInstance list(@QueryParam("success") String success,
                                 @QueryParam("error") String error) {
        TemplateInstance template = listTemplate.data("products", productUseCase.listProducts());
        return withMessages(template, success, error);
    }

    @GET
    @Path("/buscar")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Buscar productos", description = "Por código de barras o por el inicio del nombre (máximo 8 resultados activos)")
    public List<ProductDto> search(@QueryParam("q") String q) {
        return productUseCase.searchProducts(q).stream().map(ProductDtoMapper::toDto).toList();
    }

    @POST
    @Path("/crear")
    @RolesAllowed("admin")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(summary = "Registrar producto", description = "Valida el código (único), el nombre y el precio")
    public Response create(@FormParam("code") String code,
                           @FormParam("name") String name,
                           @FormParam("unitPrice") String unitPrice) {
        try {
            BigDecimal price;
            try {
                price = new BigDecimal(unitPrice == null ? "" : unitPrice.trim());
            } catch (NumberFormatException e) {
                throw new BusinessException("El precio no es un número válido.");
            }
            Product saved = productUseCase.createProduct(new Product(code, name, price));
            return redirect("/productos", "Producto \"" + saved.getName() + "\" registrado correctamente.", null);
        } catch (BusinessException e) {
            return redirect("/productos", null, e.getMessage());
        }
    }

    @POST
    @Path("/{id}/estado")
    @RolesAllowed("admin")
    @Operation(summary = "Activar o desactivar producto", description = "Un producto inactivo no aparece en la búsqueda ni se puede vender")
    public Response toggle(@PathParam("id") Long id) {
        try {
            Product product = productUseCase.toggleActive(id);
            return redirect("/productos",
                    "Producto \"" + product.getName() + "\" " + (product.isActive() ? "activado." : "desactivado."), null);
        } catch (BusinessException e) {
            return redirect("/productos", null, e.getMessage());
        }
    }

    @POST
    @Path("/{id}/editar")
    @RolesAllowed("admin")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(summary = "Modificar producto", description = "Solo el administrador: cambia el nombre y el precio (el código no cambia)")
    public Response update(@PathParam("id") Long id,
                           @FormParam("name") String name,
                           @FormParam("unitPrice") String unitPrice) {
        try {
            BigDecimal price;
            try {
                price = new BigDecimal(unitPrice == null ? "" : unitPrice.trim());
            } catch (NumberFormatException e) {
                throw new BusinessException("El precio no es un número válido.");
            }
            Product saved = productUseCase.updateProduct(id, name, price);
            return redirect("/productos", "Producto \"" + saved.getName() + "\" actualizado.", null);
        } catch (BusinessException e) {
            return redirect("/productos", null, e.getMessage());
        }
    }
}