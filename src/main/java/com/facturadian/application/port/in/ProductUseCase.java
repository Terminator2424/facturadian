package com.facturadian.application.port.in;

import com.facturadian.domain.model.Product;
import java.math.BigDecimal;
import java.util.List;

public interface ProductUseCase {
    Product createProduct(Product product);
    Product getProduct(Long id);
    List<Product> listProducts();
    List<Product> listActiveProducts();
    /** Busca por código de barras o por el inicio del nombre (ej. "li" encuentra "Libro"). */
    List<Product> searchProducts(String query);
    Product toggleActive(Long id);
    Product updateProduct(Long id, String name, BigDecimal unitPrice);
}