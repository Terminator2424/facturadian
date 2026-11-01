package com.facturadian.domain.port.output;

import com.facturadian.domain.model.Client;
import java.util.List;
import java.util.Optional;

public interface ClientRepositoryPort {
    Client save(Client client);
    Optional<Client> findById(Long id);
    Optional<Client> findByNit(String nit);
    List<Client> findAll();
}