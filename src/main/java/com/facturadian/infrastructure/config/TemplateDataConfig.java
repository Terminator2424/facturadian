package com.facturadian.infrastructure.config;

import com.facturadian.domain.model.Client;
import com.facturadian.domain.model.Invoice;
import com.facturadian.domain.model.InvoiceItem;
import com.facturadian.domain.model.InvoiceStatus;
import com.facturadian.domain.model.SalesSummary;
import io.quarkus.qute.TemplateData;

/** Registra las clases del dominio para Qute sin tocar el dominio. */
@TemplateData(target = Invoice.class)
@TemplateData(target = InvoiceItem.class)
@TemplateData(target = Client.class)
@TemplateData(target = InvoiceStatus.class)
@TemplateData(target = SalesSummary.class)
public class TemplateDataConfig {
}