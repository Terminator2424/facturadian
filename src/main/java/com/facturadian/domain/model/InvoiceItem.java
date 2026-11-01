package com.facturadian.domain.model;

import com.facturadian.domain.exception.BusinessException;
import java.math.BigDecimal;

public class InvoiceItem {
    public static final BigDecimal MAX_SUBTOTAL = new BigDecimal("10000000000"); // 10 mil millones por ítem
    public static final int MAX_QUANTITY = 100000;

    private Long id;
    private String productName;
    private BigDecimal unitPrice;
    private int quantity;

    public InvoiceItem() {}

    public InvoiceItem(String productName, BigDecimal unitPrice, int quantity) {
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    /** Regla de negocio: valida los datos del ítem. */
    public void validate() {
        if (productName == null || productName.isBlank()) {
            throw new BusinessException("El nombre del producto es obligatorio.");
        }
        productName = productName.trim();
        if (productName.length() > 150) {
            throw new BusinessException("El nombre del producto no puede superar 150 caracteres.");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El precio unitario de \"" + productName + "\" debe ser mayor que cero.");
        }
        if (unitPrice.scale() > 2) {
            throw new BusinessException("El precio de \"" + productName + "\" admite máximo 2 decimales.");
        }
        if (quantity <= 0) {
            throw new BusinessException("La cantidad de \"" + productName + "\" debe ser mayor que cero.");
        }
        if (quantity > MAX_QUANTITY) {
            throw new BusinessException("La cantidad de \"" + productName + "\" no puede superar " + MAX_QUANTITY + ".");
        }
        if (getSubtotal().compareTo(MAX_SUBTOTAL) > 0) {
            throw new BusinessException("El valor del ítem \"" + productName + "\" supera el máximo permitido.");
        }
    }

    public BigDecimal getSubtotal() {
        return unitPrice == null ? BigDecimal.ZERO : unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}