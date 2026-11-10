package com.facturadian.application.port.out;

import com.facturadian.domain.model.Product;
import java.util.List;
import java.util.Optional;

public interface ProductRepositoryPort {
    Product save(Product product);
    Optional<Product> findById(Long id);
    Optional<Product> findByCode(String code);
    List<Product> findAll();
    List<Product> search(String query, int limit);
}