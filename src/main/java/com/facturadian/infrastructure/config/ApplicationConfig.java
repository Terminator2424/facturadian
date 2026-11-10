package com.facturadian.infrastructure.config;

import com.facturadian.application.port.in.ClientUseCase;
import com.facturadian.application.port.in.InvoiceUseCase;
import com.facturadian.application.port.in.UserUseCase;
import com.facturadian.application.port.in.ProductUseCase;
import com.facturadian.application.port.out.ClientRepositoryPort;
import com.facturadian.application.port.out.InvoiceRepositoryPort;
import com.facturadian.application.port.out.PasswordHasherPort;
import com.facturadian.application.port.out.PdfGeneratorPort;
import com.facturadian.application.port.out.ProductRepositoryPort;
import com.facturadian.application.port.out.UserRepositoryPort;
import com.facturadian.application.usecase.ClientService;
import com.facturadian.application.usecase.InvoiceService;
import com.facturadian.application.usecase.UserService;
import com.facturadian.application.usecase.ProductService;

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
                                         PdfGeneratorPort pdf,
                                         ProductRepositoryPort products) {
        return new InvoiceService(invoices, clients, users, pdf, products);
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

    @Produces
    @ApplicationScoped
    public ProductUseCase productUseCase(ProductRepositoryPort products) {
        return new ProductService(products);
    }
}