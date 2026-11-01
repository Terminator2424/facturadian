package com.facturadian.domain.model;

import com.facturadian.domain.exception.BusinessException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Invoice {
    public static final BigDecimal IVA_RATE = new BigDecimal("0.19");
    public static final BigDecimal RETE_IVA_RATE = new BigDecimal("0.15");

    private Long id;
    private String invoiceNumber;
    private LocalDate issueDate = LocalDate.now();
    private InvoiceStatus status = InvoiceStatus.ISSUED;
    private Client client;
    private AppUser issuedBy;
    private List<InvoiceItem> items = new ArrayList<>();
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal iva = BigDecimal.ZERO;
    private BigDecimal reteIva = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;

    public Invoice() {}

    public Invoice(String invoiceNumber, Client client) {
        this.invoiceNumber = invoiceNumber;
        this.client = client;
    }

    /** Regla: solo se agregan ítems a facturas Emitidas. */
    public void addItem(InvoiceItem item) {
        if (status != InvoiceStatus.ISSUED) {
            throw new BusinessException("Solo se pueden agregar ítems a facturas en estado Emitida.");
        }
        if (item == null) {
            throw new BusinessException("El ítem es obligatorio.");
        }
        if (items.size() >= 50) {
            throw new BusinessException("Una factura admite máximo 50 ítems.");
        }
        item.validate();
        items.add(item);
        recalculateTotals();
    }

    /** Regla: solo una factura Emitida se puede marcar como Pagada. */
    public void markAsPaid() {
        if (status != InvoiceStatus.ISSUED) {
            throw new BusinessException("Solo se puede pagar una factura en estado Emitida.");
        }
        this.status = InvoiceStatus.PAID;
    }

    /** Regla: una factura Pagada o ya Anulada no se puede anular. */
    public void cancel() {
        if (status == InvoiceStatus.PAID) {
            throw new BusinessException("Una factura pagada no se puede anular.");
        }
        if (status == InvoiceStatus.CANCELLED) {
            throw new BusinessException("La factura ya está anulada.");
        }
        this.status = InvoiceStatus.CANCELLED;
    }

    /** Regla DIAN: IVA 19%; si el cliente es agente retenedor, ReteIVA = 15% del IVA. */
    public void recalculateTotals() {
        this.subtotal = items.stream()
                .map(InvoiceItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        this.iva = subtotal.multiply(IVA_RATE).setScale(2, RoundingMode.HALF_UP);
        if (client != null && client.isWithholdingAgent()) {
            this.reteIva = iva.multiply(RETE_IVA_RATE).setScale(2, RoundingMode.HALF_UP);
        } else {
            this.reteIva = BigDecimal.ZERO.setScale(2);
        }
        this.total = subtotal.add(iva).subtract(reteIva).setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }
    public Client getClient() { return client; }
    public AppUser getIssuedBy() { return issuedBy; }
    public void setIssuedBy(AppUser issuedBy) { this.issuedBy = issuedBy; }

    /** Nombre del cajero que emitió la factura (las facturas antiguas no lo tienen). */
    public String getIssuedByName() {
        return issuedBy == null ? "Sin registrar" : issuedBy.getFullName();
    }
    public void setClient(Client client) { this.client = client; }
    public List<InvoiceItem> getItems() { return items; }
    public void setItems(List<InvoiceItem> items) { this.items = items; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getIva() { return iva; }
    public void setIva(BigDecimal iva) { this.iva = iva; }
    public BigDecimal getReteIva() { return reteIva; }
    public void setReteIva(BigDecimal reteIva) { this.reteIva = reteIva; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}