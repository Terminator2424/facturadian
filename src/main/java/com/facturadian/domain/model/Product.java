package com.facturadian.domain.model;

import com.facturadian.domain.exception.BusinessException;
import java.math.BigDecimal;

/** Producto del catálogo. El cajero lo encuentra por código de barras o por nombre. */
public class Product {
    public static final BigDecimal MAX_PRICE = new BigDecimal("10000000000");

    private Long id;
    private String code;
    private String name;
    private BigDecimal unitPrice;
    private boolean active = true;

    public Product() {}

    public Product(String code, String name, BigDecimal unitPrice) {
        this.code = code;
        this.name = name;
        this.unitPrice = unitPrice;
    }

    /** Regla de negocio: valida los datos del producto. */
    public void validate() {
        if (code == null || code.isBlank()) {
            throw new BusinessException("El código del producto es obligatorio.");
        }
        code = code.trim().toUpperCase();
        if (!code.matches("[A-Z0-9\\-]{3,30}")) {
            throw new BusinessException("El código solo puede tener letras, números y guion (3 a 30 caracteres).");
        }
        if (name == null || name.isBlank()) {
            throw new BusinessException("El nombre del producto es obligatorio.");
        }
        name = name.trim();
        if (name.length() > 150) {
            throw new BusinessException("El nombre del producto no puede superar 150 caracteres.");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El precio debe ser mayor que cero.");
        }
        if (unitPrice.scale() > 2) {
            throw new BusinessException("El precio admite máximo 2 decimales.");
        }
        if (unitPrice.compareTo(MAX_PRICE) > 0) {
            throw new BusinessException("El precio supera el máximo permitido.");
        }
    }

    public void toggleActive() {
        this.active = !this.active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}