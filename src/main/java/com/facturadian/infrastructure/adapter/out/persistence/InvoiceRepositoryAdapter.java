package com.facturadian.infrastructure.adapter.out.persistence;

import com.facturadian.domain.exception.DuplicateInvoiceNumberException;
import com.facturadian.domain.model.Invoice;
import com.facturadian.application.port.out.InvoiceRepositoryPort;
import com.facturadian.infrastructure.adapter.out.persistence.entity.ClientEntity;
import com.facturadian.infrastructure.adapter.out.persistence.entity.InvoiceEntity;
import com.facturadian.infrastructure.adapter.out.persistence.entity.ProductEntity;
import com.facturadian.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.facturadian.infrastructure.adapter.out.persistence.mapper.InvoiceMapper;
import com.facturadian.infrastructure.adapter.out.persistence.repository.ClientPanacheRepository;
import com.facturadian.infrastructure.adapter.out.persistence.repository.InvoicePanacheRepository;
import com.facturadian.infrastructure.adapter.out.persistence.repository.UserPanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.hibernate.exception.ConstraintViolationException;

/**
 * @Transactional en la clase: el mapeo a dominio ocurre dentro de la transacción,
 * así los ítems (carga perezosa) se leen sin LazyInitializationException.
 */
@ApplicationScoped
@Transactional
public class InvoiceRepositoryAdapter implements InvoiceRepositoryPort {

    @Inject
    InvoicePanacheRepository invoiceRepository;

    @Inject
    ClientPanacheRepository clientRepository;

    @Inject
    UserPanacheRepository userRepository;

    @Inject
    EntityManager em;

    @Override
    public Invoice save(Invoice invoice) {
        ClientEntity client = clientRepository.findById(invoice.getClient().getId());
        UserEntity issuedBy = invoice.getIssuedBy() == null ? null : userRepository.findById(invoice.getIssuedBy().getId());
        InvoiceEntity entity = InvoiceMapper.toEntity(invoice, client, issuedBy);
        for (int i = 0; i < invoice.getItems().size(); i++) {
            Long productId = invoice.getItems().get(i).getProductId();
            if (productId != null) {
                entity.getItems().get(i).setProduct(em.getReference(ProductEntity.class, productId));
            }
        }
        try {
            // merge sirve tanto para facturas nuevas (id null) como para actualizar existentes
            InvoiceEntity saved = em.merge(entity);
            em.flush();
            return InvoiceMapper.toDomain(saved);
        } catch (PersistenceException e) {
            // Factura nueva rechazada por la restricción unique: el número ya lo tomó otro cajero
            if (invoice.getId() == null && isConstraintViolation(e)) {
                throw new DuplicateInvoiceNumberException();
            }
            throw e;
        }
    }

    @Override
    public Optional<Invoice> findById(Long id) {
        return invoiceRepository.findByIdOptional(id).map(InvoiceMapper::toDomain);
    }

    @Override
    public List<Invoice> findAll() {
        return invoiceRepository.list("order by id desc").stream().map(InvoiceMapper::toDomain).toList();
    }

    @Override
    public List<Invoice> findByIssueDateBetween(LocalDate from, LocalDate to) {
        return invoiceRepository.list("issueDate between ?1 and ?2 order by issueDate, id", from, to)
                .stream().map(InvoiceMapper::toDomain).toList();
    }

    @Override
    public long count() {
        return invoiceRepository.count();
    }

    private static boolean isConstraintViolation(Throwable error) {
        for (Throwable t = error; t != null; t = t.getCause()) {
            if (t instanceof ConstraintViolationException || t instanceof SQLIntegrityConstraintViolationException) {
                return true;
            }
        }
        return false;
    }
}