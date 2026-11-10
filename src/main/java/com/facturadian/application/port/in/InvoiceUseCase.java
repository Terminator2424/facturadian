package com.facturadian.application.port.in;

import com.facturadian.domain.model.Invoice;
import com.facturadian.domain.model.InvoiceStatus;
import com.facturadian.domain.model.SalesSummary;
import java.time.LocalDate;
import java.util.List;

public interface InvoiceUseCase {

    /** Línea de venta: el producto sale del catálogo (con su precio), solo se elige la cantidad. */
    record ItemRequest(Long productId, int quantity) {}

    Invoice createInvoice(Long clientId, List<ItemRequest> items, String issuedByUsername);
    Invoice addItem(Long invoiceId, ItemRequest item);
    Invoice markAsPaid(Long invoiceId);
    Invoice cancelInvoice(Long invoiceId);
    Invoice getInvoice(Long invoiceId);
    List<Invoice> listInvoices(InvoiceStatus status); // status null = todas
    SalesSummary getSalesSummary(LocalDate from, LocalDate to);
    byte[] generateInvoicePdf(Long invoiceId);
    byte[] generateSummaryPdf(LocalDate from, LocalDate to);
}