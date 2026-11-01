package com.facturadian.infrastructure.adapter.out.persistence.repository;

import com.facturadian.infrastructure.adapter.out.persistence.entity.InvoiceEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InvoicePanacheRepository implements PanacheRepository<InvoiceEntity> {
}