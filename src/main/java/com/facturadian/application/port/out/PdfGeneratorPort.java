package com.facturadian.application.port.out;

import com.facturadian.domain.model.Invoice;
import com.facturadian.domain.model.SalesSummary;

public interface PdfGeneratorPort {
    byte[] generateInvoicePdf(Invoice invoice);
    byte[] generateSummaryPdf(SalesSummary summary);
}