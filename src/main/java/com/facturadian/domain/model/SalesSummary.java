package com.facturadian.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Resumen de ventas por rango de fechas. Las facturas anuladas no suman en los totales. */
public class SalesSummary {
    private final LocalDate from;
    private final LocalDate to;
    private final List<Invoice> invoices;
    private int validCount = 0;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal iva = BigDecimal.ZERO;
    private BigDecimal reteIva = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;
    private BigDecimal totalPaid = BigDecimal.ZERO;
    private BigDecimal totalPending = BigDecimal.ZERO;

    public SalesSummary(LocalDate from, LocalDate to, List<Invoice> invoices) {
        this.from = from;
        this.to = to;
        this.invoices = invoices;
        for (Invoice invoice : invoices) {
            if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
                continue;
            }
            validCount++;
            subtotal = subtotal.add(invoice.getSubtotal());
            iva = iva.add(invoice.getIva());
            reteIva = reteIva.add(invoice.getReteIva());
            total = total.add(invoice.getTotal());
            if (invoice.getStatus() == InvoiceStatus.PAID) {
                totalPaid = totalPaid.add(invoice.getTotal());
            } else {
                totalPending = totalPending.add(invoice.getTotal());
            }
        }
    }

    public LocalDate getFrom() { return from; }
    public LocalDate getTo() { return to; }
    public List<Invoice> getInvoices() { return invoices; }
    public int getValidCount() { return validCount; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getIva() { return iva; }
    public BigDecimal getReteIva() { return reteIva; }
    public BigDecimal getTotal() { return total; }
    public BigDecimal getTotalPaid() { return totalPaid; }
    public BigDecimal getTotalPending() { return totalPending; }
}