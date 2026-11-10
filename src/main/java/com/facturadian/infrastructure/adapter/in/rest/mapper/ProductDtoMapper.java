package com.facturadian.infrastructure.adapter.in.rest.mapper;

import com.facturadian.domain.model.Product;
import com.facturadian.infrastructure.adapter.in.rest.dto.ProductDto;

public final class ProductDtoMapper {
    private ProductDtoMapper() {}

    public static ProductDto toDto(Product p) {
        return new ProductDto(p.getId(), p.getCode(), p.getName(), p.getUnitPrice());
    }
}