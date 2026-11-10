package com.facturadian.infrastructure.adapter.out.persistence;

import com.facturadian.application.port.out.ProductRepositoryPort;
import com.facturadian.domain.model.Product;
import com.facturadian.infrastructure.adapter.out.persistence.entity.ProductEntity;
import com.facturadian.infrastructure.adapter.out.persistence.mapper.ProductMapper;
import com.facturadian.infrastructure.adapter.out.persistence.repository.ProductPanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class ProductRepositoryAdapter implements ProductRepositoryPort {

    @Inject
    ProductPanacheRepository repository;

    @Override
    public Product save(Product product) {
        ProductEntity entity = ProductMapper.toEntity(product);
        if (entity.getId() == null) {
            repository.persist(entity);
        } else {
            entity = repository.getEntityManager().merge(entity);
        }
        return ProductMapper.toDomain(entity);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return repository.findByIdOptional(id).map(ProductMapper::toDomain);
    }

    @Override
    public Optional<Product> findByCode(String code) {
        return repository.find("code", code).firstResultOptional().map(ProductMapper::toDomain);
    }

    @Override
    public List<Product> findAll() {
        return repository.list("order by name").stream().map(ProductMapper::toDomain).toList();
    }

    /** Productos activos cuyo código empieza por el texto, o cuyo nombre (o alguna palabra) empieza por él. */
    @Override
    public List<Product> search(String query, int limit) {
        String q = query.toLowerCase().replace("%", "").replace("_", "");
        if (q.isBlank()) {
            return List.of();
        }
        return repository.find(
                        "active = true and (lower(code) like ?1 or lower(name) like ?1 or lower(name) like ?2)",
                        Sort.by("name"), q + "%", "% " + q + "%")
                .page(Page.ofSize(limit)).list()
                .stream().map(ProductMapper::toDomain).toList();
    }
}