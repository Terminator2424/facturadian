package com.facturadian.infrastructure.config;

import com.facturadian.application.port.in.ProductUseCase;
import com.facturadian.domain.model.Product;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.math.BigDecimal;

/** Carga un catálogo de ejemplo al iniciar, solo si no hay productos. */
@ApplicationScoped
public class ProductSeeder {

    @Inject
    ProductUseCase products;

    private int n = 0;

    void onStart(@Observes StartupEvent event) {
        if (!products.listProducts().isEmpty()) {
            return;
        }
        add("Portátil Lenovo", "2500000");
        add("Mouse inalámbrico", "45000");
        add("Teclado mecánico", "180000");
        add("Taladro percutor", "320000");
        add("Caja de tornillos x100", "18500");
        add("Resma de papel carta", "14000");
        add("Libro de contabilidad", "38000");
        add("Libreta de apuntes", "6500");
        add("Lima triangular", "12000");
        add("Lijadora orbital", "210000");
        add("Lápiz HB x12", "9000");
        add("Cuaderno cuadriculado", "8500");
        add("Hosting anual", "600000");
        add("Dominio .com", "55000");
    }

    private void add(String name, String price) {
        n++;
        products.createProduct(new Product("770100000" + String.format("%04d", n), name, new BigDecimal(price)));
    }
}