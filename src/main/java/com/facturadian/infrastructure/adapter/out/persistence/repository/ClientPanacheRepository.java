package com.facturadian.infrastructure.adapter.out.persistence.repository;

import com.facturadian.infrastructure.adapter.out.persistence.entity.ClientEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ClientPanacheRepository implements PanacheRepository<ClientEntity> {
}