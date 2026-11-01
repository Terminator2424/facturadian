package com.facturadian.infrastructure.adapter.out.persistence.mapper;

import com.facturadian.domain.model.Client;
import com.facturadian.infrastructure.adapter.out.persistence.entity.ClientEntity;

public final class ClientMapper {
    private ClientMapper() {}

    public static Client toDomain(ClientEntity e) {
        if (e == null) return null;
        Client c = new Client();
        c.setId(e.getId());
        c.setNit(e.getNit());
        c.setName(e.getName());
        c.setEmail(e.getEmail());
        c.setWithholdingAgent(e.isWithholdingAgent());
        return c;
    }

    public static ClientEntity toEntity(Client d) {
        if (d == null) return null;
        ClientEntity e = new ClientEntity();
        e.setId(d.getId());
        e.setNit(d.getNit());
        e.setName(d.getName());
        e.setEmail(d.getEmail());
        e.setWithholdingAgent(d.isWithholdingAgent());
        return e;
    }
}