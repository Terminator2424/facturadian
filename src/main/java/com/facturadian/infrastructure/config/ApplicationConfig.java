package com.facturadian.infrastructure.config;

import com.facturadian.domain.port.input.ClientUseCase;
import com.facturadian.domain.port.input.InvoiceUseCase;
import com.facturadian.domain.port.input.UserUseCase;
import com.facturadian.domain.port.output.ClientRepositoryPort;
import com.facturadian.domain.port.output.InvoiceRepositoryPort;
import com.facturadian.domain.port.output.PasswordHasherPort;
import com.facturadian.domain.port.output.PdfGeneratorPort;
import com.facturadian.domain.port.output.UserRepositoryPort;
import com.facturadian.domain.service.ClientService;
import com.facturadian.domain.service.InvoiceService;
import com.facturadian.domain.service.UserService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

/**
 * Los servicios del dominio son Java puro (sin anotaciones).
 * Aquí, en infraestructura, se crean y se registran como beans.
 */
@ApplicationScoped
public class ApplicationConfig {

    @Produces
    @ApplicationScoped
    public InvoiceUseCase invoiceUseCase(InvoiceRepositoryPort invoices,
                                         ClientRepositoryPort clients,
                                         UserRepositoryPort users,
                                         PdfGeneratorPort pdf) {
        return new InvoiceService(invoices, clients, users, pdf);
    }

    @Produces
    @ApplicationScoped
    public ClientUseCase clientUseCase(ClientRepositoryPort clients) {
        return new ClientService(clients);
    }

    @Produces
    @ApplicationScoped
    public UserUseCase userUseCase(UserRepositoryPort users, PasswordHasherPort hasher) {
        return new UserService(users, hasher);
    }
}