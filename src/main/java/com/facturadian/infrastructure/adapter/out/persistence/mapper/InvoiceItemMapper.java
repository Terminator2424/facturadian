package com.facturadian.infrastructure.adapter.out.persistence.mapper;

import com.facturadian.domain.model.InvoiceItem;
import com.facturadian.infrastructure.adapter.out.persistence.entity.InvoiceEntity;
import com.facturadian.infrastructure.adapter.out.persistence.entity.InvoiceItemEntity;

public final class InvoiceItemMapper {
    private InvoiceItemMapper() {}

    public static InvoiceItem toDomain(InvoiceItemEntity e) {
        InvoiceItem d = new InvoiceItem();
        d.setId(e.getId());
        d.setProductName(e.getProductName());
        d.setUnitPrice(e.getUnitPrice());
        d.setQuantity(e.getQuantity());
        return d;
    }

    /** Se recibe la factura dueña para llenar la llave foránea invoice_id. */
    public static InvoiceItemEntity toEntity(InvoiceItem d, InvoiceEntity owner) {
        InvoiceItemEntity e = new InvoiceItemEntity();
        e.setId(d.getId());
        e.setProductName(d.getProductName());
        e.setUnitPrice(d.getUnitPrice());
        e.setQuantity(d.getQuantity());
        e.setInvoice(owner);
        return e;
    }
}