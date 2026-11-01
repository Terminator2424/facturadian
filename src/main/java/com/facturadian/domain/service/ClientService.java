package com.facturadian.domain.service;

import com.facturadian.domain.exception.BusinessException;
import com.facturadian.domain.model.Client;
import com.facturadian.domain.port.input.ClientUseCase;
import com.facturadian.domain.port.output.ClientRepositoryPort;
import java.util.List;
import java.util.regex.Pattern;

public class ClientService implements ClientUseCase {

    private static final Pattern NIT = Pattern.compile("^[0-9.\\-]{5,20}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final ClientRepositoryPort clientRepository;

    public ClientService(ClientRepositoryPort clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public Client createClient(Client client) {
        if (client.getNit() == null || client.getNit().isBlank()) {
            throw new BusinessException("El NIT es obligatorio.");
        }
        if (client.getName() == null || client.getName().isBlank()) {
            throw new BusinessException("El nombre del cliente es obligatorio.");
        }
        client.setNit(client.getNit().trim());
        client.setName(client.getName().trim());
        if (!NIT.matcher(client.getNit()).matches()) {
            throw new BusinessException("El NIT solo puede tener números, puntos y guion (5 a 20 caracteres).");
        }
        if (client.getName().length() > 120) {
            throw new BusinessException("El nombre no puede superar 120 caracteres.");
        }
        if (client.getEmail() != null && !client.getEmail().isBlank()) {
            client.setEmail(client.getEmail().trim());
            if (client.getEmail().length() > 120 || !EMAIL.matcher(client.getEmail()).matches()) {
                throw new BusinessException("El correo electrónico no es válido.");
            }
        } else {
            client.setEmail(null);
        }
        if (clientRepository.findByNit(client.getNit()).isPresent()) {
            throw new BusinessException("Ya existe un cliente con el NIT " + client.getNit() + ".");
        }
        return clientRepository.save(client);
    }

    @Override
    public Client getClient(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Cliente no encontrado."));
    }

    @Override
    public List<Client> listClients() {
        return clientRepository.findAll();
    }
}