package com.facturadian.domain.model;

public enum InvoiceStatus {
    ISSUED("Emitida"),
    PAID("Pagada"),
    CANCELLED("Anulada");

    private final String label;

    InvoiceStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}