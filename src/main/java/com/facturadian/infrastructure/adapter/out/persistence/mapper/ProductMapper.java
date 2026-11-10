package com.facturadian.infrastructure.adapter.out.persistence.mapper;

import com.facturadian.domain.model.Product;
import com.facturadian.infrastructure.adapter.out.persistence.entity.ProductEntity;

public final class ProductMapper {
    private ProductMapper() {}

    public static Product toDomain(ProductEntity e) {
        if (e == null) return null;
        Product p = new Product();
        p.setId(e.getId());
        p.setCode(e.getCode());
        p.setName(e.getName());
        p.setUnitPrice(e.getUnitPrice());
        p.setActive(e.isActive());
        return p;
    }

    public static ProductEntity toEntity(Product d) {
        if (d == null) return null;
        ProductEntity e = new ProductEntity();
        e.setId(d.getId());
        e.setCode(d.getCode());
        e.setName(d.getName());
        e.setUnitPrice(d.getUnitPrice());
        e.setActive(d.isActive());
        return e;
    }
}