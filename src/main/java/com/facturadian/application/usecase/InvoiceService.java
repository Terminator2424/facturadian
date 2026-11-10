package com.facturadian.application.usecase;

import com.facturadian.domain.exception.BusinessException;
import com.facturadian.domain.exception.DuplicateInvoiceNumberException;
import com.facturadian.domain.model.AppUser;
import com.facturadian.domain.model.Client;
import com.facturadian.domain.model.Invoice;
import com.facturadian.domain.model.InvoiceItem;
import com.facturadian.domain.model.InvoiceStatus;
import com.facturadian.domain.model.Product;
import com.facturadian.domain.model.SalesSummary;
import com.facturadian.application.port.in.InvoiceUseCase;
import com.facturadian.application.port.out.ClientRepositoryPort;
import com.facturadian.application.port.out.InvoiceRepositoryPort;
import com.facturadian.application.port.out.PdfGeneratorPort;
import com.facturadian.application.port.out.ProductRepositoryPort;
import com.facturadian.application.port.out.UserRepositoryPort;
import java.time.LocalDate;
import java.util.List;

public class InvoiceService implements InvoiceUseCase {

    private static final int MAX_NUMBER_ATTEMPTS = 5;

    private final InvoiceRepositoryPort invoiceRepository;
    private final ClientRepositoryPort clientRepository;
    private final UserRepositoryPort userRepository;
    private final PdfGeneratorPort pdfGenerator;
    private final ProductRepositoryPort productRepository;

    public InvoiceService(InvoiceRepositoryPort invoiceRepository,
                          ClientRepositoryPort clientRepository,
                          UserRepositoryPort userRepository,
                          PdfGeneratorPort pdfGenerator,
                          ProductRepositoryPort productRepository) {
        this.invoiceRepository = invoiceRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.pdfGenerator = pdfGenerator;
        this.productRepository = productRepository;
    }

    @Override
    public Invoice createInvoice(Long clientId, List<ItemRequest> items, String issuedByUsername) {
        if (clientId == null) {
            throw new BusinessException("Debe seleccionar un cliente.");
        }
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new BusinessException("Cliente no encontrado."));
        AppUser cashier = userRepository
                .findByUsername(issuedByUsername == null ? "" : issuedByUsername.trim().toLowerCase())
                .orElseThrow(() -> new BusinessException("Tu sesión no es válida. Inicia sesión de nuevo."));
        if (items == null || items.isEmpty()) {
            throw new BusinessException("La factura debe tener al menos un ítem.");
        }
        Invoice invoice = new Invoice(null, client);
        invoice.setIssuedBy(cashier);
        invoice.setIssueDate(LocalDate.now());
        for (ItemRequest line : items) {
            invoice.addItem(toItem(line));
        }
        return saveWithNextNumber(invoice);
    }

    /**
     * Si dos cajeros facturan a la vez y reciben el mismo número, la restricción unique de la base
     * rechaza al segundo y aquí se reintenta con el número siguiente.
     */
    private Invoice saveWithNextNumber(Invoice invoice) {
        for (int attempt = 1; attempt <= MAX_NUMBER_ATTEMPTS; attempt++) {
            invoice.setInvoiceNumber(nextInvoiceNumber());
            try {
                return invoiceRepository.save(invoice);
            } catch (DuplicateInvoiceNumberException e) {
                // Otro cajero tomó ese número primero: se vuelve a calcular y se reintenta.
            }
        }
        throw new BusinessException("No se pudo asignar el número de factura. Intenta de nuevo.");
    }

    @Override
    public Invoice addItem(Long invoiceId, ItemRequest item) {
        Invoice invoice = getInvoice(invoiceId);
        invoice.addItem(toItem(item));
        return invoiceRepository.save(invoice);
    }

    @Override
    public Invoice markAsPaid(Long invoiceId) {
        Invoice invoice = getInvoice(invoiceId);
        invoice.markAsPaid();
        return invoiceRepository.save(invoice);
    }

    @Override
    public Invoice cancelInvoice(Long invoiceId) {
        Invoice invoice = getInvoice(invoiceId);
        invoice.cancel();
        return invoiceRepository.save(invoice);
    }

    @Override
    public Invoice getInvoice(Long invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new BusinessException("Factura no encontrada."));
    }

    @Override
    public List<Invoice> listInvoices(InvoiceStatus status) {
        List<Invoice> all = invoiceRepository.findAll();
        if (status == null) {
            return all;
        }
        return all.stream().filter(i -> i.getStatus() == status).toList();
    }

    @Override
    public SalesSummary getSalesSummary(LocalDate from, LocalDate to) {
        LocalDate today = LocalDate.now();
        LocalDate start = from != null ? from : today.withDayOfMonth(1);
        LocalDate end = to != null ? to : today;
        if (start.isAfter(end)) {
            throw new BusinessException("La fecha inicial no puede ser posterior a la fecha final.");
        }
        return new SalesSummary(start, end, invoiceRepository.findByIssueDateBetween(start, end));
    }

    @Override
    public byte[] generateInvoicePdf(Long invoiceId) {
        return pdfGenerator.generateInvoicePdf(getInvoice(invoiceId));
    }

    @Override
    public byte[] generateSummaryPdf(LocalDate from, LocalDate to) {
        return pdfGenerator.generateSummaryPdf(getSalesSummary(from, to));
    }

    private InvoiceItem toItem(ItemRequest line) {
        if (line == null || line.productId() == null) {
            throw new BusinessException("Debe seleccionar un producto.");
        }
        Product product = productRepository.findById(line.productId())
                .orElseThrow(() -> new BusinessException("Producto no encontrado."));
        if (!product.isActive()) {
            throw new BusinessException("El producto \"" + product.getName() + "\" está inactivo.");
        }
        return new InvoiceItem(product.getName(), product.getUnitPrice(), line.quantity());
    }

    private String nextInvoiceNumber() {
        return "FV-" + String.format("%06d", invoiceRepository.count() + 1);
    }
}