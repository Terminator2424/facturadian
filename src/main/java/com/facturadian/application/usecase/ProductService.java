package com.facturadian.application.usecase;

import com.facturadian.application.port.in.ProductUseCase;
import com.facturadian.application.port.out.ProductRepositoryPort;
import com.facturadian.domain.exception.BusinessException;
import com.facturadian.domain.model.Product;
import java.math.BigDecimal;
import java.util.List;

public class ProductService implements ProductUseCase {

    private static final int SEARCH_LIMIT = 8;

    private final ProductRepositoryPort repository;

    public ProductService(ProductRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Product createProduct(Product product) {
        if (product == null) {
            throw new BusinessException("El producto es obligatorio.");
        }
        product.validate();
        if (repository.findByCode(product.getCode()).isPresent()) {
            throw new BusinessException("Ya existe un producto con el código " + product.getCode() + ".");
        }
        product.setActive(true);
        return repository.save(product);
    }

    @Override
    public Product getProduct(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("Producto no encontrado."));
    }

    @Override
    public List<Product> listProducts() {
        return repository.findAll();
    }

    @Override
    public List<Product> listActiveProducts() {
        return repository.findAll().stream().filter(Product::isActive).toList();
    }

    @Override
    public List<Product> searchProducts(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return repository.search(query.trim(), SEARCH_LIMIT);
    }

    @Override
    public Product toggleActive(Long id) {
        Product product = getProduct(id);
        product.toggleActive();
        return repository.save(product);
    }

    @Override
    public Product updateProduct(Long id, String name, BigDecimal unitPrice) {
        Product product = getProduct(id);
        product.setName(name);
        product.setUnitPrice(unitPrice);
        product.validate();
        return repository.save(product);
    }
}