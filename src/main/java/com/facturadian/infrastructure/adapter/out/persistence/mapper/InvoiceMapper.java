package com.facturadian.infrastructure.adapter.out.persistence.mapper;

import com.facturadian.domain.model.Invoice;
import com.facturadian.domain.model.InvoiceItem;
import com.facturadian.infrastructure.adapter.out.persistence.entity.ClientEntity;
import com.facturadian.infrastructure.adapter.out.persistence.entity.InvoiceEntity;
import com.facturadian.infrastructure.adapter.out.persistence.entity.UserEntity;
import java.util.ArrayList;

public final class InvoiceMapper {
    private InvoiceMapper() {}

    public static Invoice toDomain(InvoiceEntity e) {
        if (e == null) return null;
        Invoice d = new Invoice();
        d.setId(e.getId());
        d.setInvoiceNumber(e.getInvoiceNumber());
        d.setIssueDate(e.getIssueDate());
        d.setStatus(e.getStatus());
        d.setClient(ClientMapper.toDomain(e.getClient()));
        d.setIssuedBy(UserMapper.toDomain(e.getIssuedBy()));
        d.setItems(new ArrayList<>(e.getItems().stream().map(InvoiceItemMapper::toDomain).toList()));
        d.setSubtotal(e.getSubtotal());
        d.setIva(e.getIva());
        d.setReteIva(e.getReteIva());
        d.setTotal(e.getTotal());
        return d;
    }

    /** El cliente y el cajero llegan ya como entidades administradas por JPA. */
    public static InvoiceEntity toEntity(Invoice d, ClientEntity client, UserEntity issuedBy) {
        InvoiceEntity e = new InvoiceEntity();
        e.setId(d.getId());
        e.setInvoiceNumber(d.getInvoiceNumber());
        e.setIssueDate(d.getIssueDate());
        e.setStatus(d.getStatus());
        e.setClient(client);
        e.setIssuedBy(issuedBy);
        e.setSubtotal(d.getSubtotal());
        e.setIva(d.getIva());
        e.setReteIva(d.getReteIva());
        e.setTotal(d.getTotal());
        for (InvoiceItem item : d.getItems()) {
            e.getItems().add(InvoiceItemMapper.toEntity(item, e));
        }
        return e;
    }
}