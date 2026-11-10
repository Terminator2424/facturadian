package com.facturadian.infrastructure.config;

import com.facturadian.domain.model.AppUser;
import com.facturadian.domain.model.Client;
import com.facturadian.domain.model.Invoice;
import com.facturadian.domain.model.InvoiceItem;
import com.facturadian.domain.model.InvoiceStatus;
import com.facturadian.application.port.in.ClientUseCase;
import com.facturadian.application.port.in.UserUseCase;
import com.facturadian.application.port.out.InvoiceRepositoryPort;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Carga datos de prueba coherentes al iniciar, solo si la base está vacía. */
@ApplicationScoped
public class DataSeeder {

    @Inject
    ClientUseCase clientUseCase;

    @Inject
    UserUseCase userUseCase;

    @Inject
    InvoiceRepositoryPort invoiceRepository;

    void onStart(@Observes StartupEvent event) {
        if (!clientUseCase.listClients().isEmpty()) {
            return;
        }

        AppUser cashier = seedCashier();

        Client boyaca = clientUseCase.createClient(
                new Client("900123456-1", "Distribuidora Boyacá S.A.S.", "compras@distriboyaca.co", true));
        Client tornillo = clientUseCase.createClient(
                new Client("800987654-3", "Ferretería El Tornillo Ltda.", "contacto@eltornillo.co", false));
        Client digital = clientUseCase.createClient(
                new Client("901555222-7", "Corporación Tunja Digital", "admin@tunjadigital.co", true));
        Client papeleria = clientUseCase.createClient(
                new Client("1049612345", "Papelería Santo Tomás", "ventas@papeleriast.co", false));

        createInvoice(boyaca, cashier, 20, InvoiceStatus.PAID,
                item("Portátil Lenovo", "2500000", 2),
                item("Mouse inalámbrico", "45000", 5));
        createInvoice(tornillo, cashier, 12, InvoiceStatus.PAID,
                item("Taladro percutor", "320000", 3),
                item("Caja de tornillos x100", "18500", 10));
        createInvoice(digital, cashier, 5, InvoiceStatus.ISSUED,
                item("Hosting anual", "600000", 1),
                item("Dominio .com", "55000", 2));
        createInvoice(papeleria, cashier, 2, InvoiceStatus.ISSUED,
                item("Resma de papel carta", "14000", 20));
        createInvoice(boyaca, cashier, 0, InvoiceStatus.CANCELLED,
                item("Teclado mecánico", "180000", 4));
    }

    /** Cajero de las facturas de prueba. Su clave es aleatoria: nadie puede iniciar sesión con él. */
    private AppUser seedCashier() {
        String password = "Aa1*" + UUID.randomUUID();
        return userUseCase.findActiveUser("sistema").orElseGet(() ->
                userUseCase.register("sistema", "Datos de prueba", "sistema@facturadian.com", password, password));
    }

    private void createInvoice(Client client, AppUser cashier, int daysAgo, InvoiceStatus finalStatus, InvoiceItem... items) {
        Invoice invoice = new Invoice("FV-" + String.format("%06d", invoiceRepository.count() + 1), client);
        invoice.setIssuedBy(cashier);
        invoice.setIssueDate(LocalDate.now().minusDays(daysAgo));
        for (InvoiceItem item : items) {
            invoice.addItem(item);
        }
        if (finalStatus == InvoiceStatus.PAID) {
            invoice.markAsPaid();
        } else if (finalStatus == InvoiceStatus.CANCELLED) {
            invoice.cancel();
        }
        invoiceRepository.save(invoice);
    }

    private InvoiceItem item(String name, String price, int quantity) {
        return new InvoiceItem(name, new BigDecimal(price), quantity);
    }
}