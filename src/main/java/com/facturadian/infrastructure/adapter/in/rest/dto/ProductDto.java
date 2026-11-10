package com.facturadian.infrastructure.adapter.in.rest.dto;

import java.math.BigDecimal;

/** Lo que el navegador recibe al buscar un producto. */
public record ProductDto(Long id, String code, String name, BigDecimal unitPrice) {
}