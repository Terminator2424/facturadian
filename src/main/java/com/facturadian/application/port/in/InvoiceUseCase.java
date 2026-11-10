package com.facturadian.application.port.in;

import com.facturadian.domain.model.Invoice;
import com.facturadian.domain.model.InvoiceItem;
import com.facturadian.domain.model.InvoiceStatus;
import com.facturadian.domain.model.SalesSummary;
import java.time.LocalDate;
import java.util.List;

public interface InvoiceUseCase {
    Invoice createInvoice(Long clientId, List<InvoiceItem> items, String issuedByUsername);
    Invoice addItem(Long invoiceId, InvoiceItem item);
    Invoice markAsPaid(Long invoiceId);
    Invoice cancelInvoice(Long invoiceId);
    Invoice getInvoice(Long invoiceId);
    List<Invoice> listInvoices(InvoiceStatus status); // status null = todas
    SalesSummary getSalesSummary(LocalDate from, LocalDate to);
    byte[] generateInvoicePdf(Long invoiceId);
    byte[] generateSummaryPdf(LocalDate from, LocalDate to);
}