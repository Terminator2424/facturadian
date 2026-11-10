package com.facturadian.application.port.in;

import com.facturadian.domain.model.Client;
import java.util.List;

public interface ClientUseCase {
    Client createClient(Client client);
    Client getClient(Long id);
    List<Client> listClients();
}