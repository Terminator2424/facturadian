package com.facturadian.domain.port.output;

import com.facturadian.domain.model.Invoice;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepositoryPort {
    Invoice save(Invoice invoice);
    Optional<Invoice> findById(Long id);
    List<Invoice> findAll();
    List<Invoice> findByIssueDateBetween(LocalDate from, LocalDate to);
    long count();
}