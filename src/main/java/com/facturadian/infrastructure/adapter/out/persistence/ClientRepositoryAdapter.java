package com.facturadian.infrastructure.adapter.out.persistence;

import com.facturadian.domain.model.Client;
import com.facturadian.domain.port.output.ClientRepositoryPort;
import com.facturadian.infrastructure.adapter.out.persistence.entity.ClientEntity;
import com.facturadian.infrastructure.adapter.out.persistence.mapper.ClientMapper;
import com.facturadian.infrastructure.adapter.out.persistence.repository.ClientPanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class ClientRepositoryAdapter implements ClientRepositoryPort {

    @Inject
    ClientPanacheRepository repository;

    @Override
    public Client save(Client client) {
        ClientEntity entity = ClientMapper.toEntity(client);
        if (entity.getId() == null) {
            repository.persist(entity);
        } else {
            entity = repository.getEntityManager().merge(entity);
        }
        return ClientMapper.toDomain(entity);
    }

    @Override
    public Optional<Client> findById(Long id) {
        return repository.findByIdOptional(id).map(ClientMapper::toDomain);
    }

    @Override
    public Optional<Client> findByNit(String nit) {
        return repository.find("nit", nit).firstResultOptional().map(ClientMapper::toDomain);
    }

    @Override
    public List<Client> findAll() {
        return repository.list("order by name").stream().map(ClientMapper::toDomain).toList();
    }
}