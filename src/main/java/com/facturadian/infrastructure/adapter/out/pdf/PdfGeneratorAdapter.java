package com.facturadian.infrastructure.adapter.out.pdf;

import com.facturadian.domain.exception.BusinessException;
import com.facturadian.domain.model.Invoice;
import com.facturadian.domain.model.InvoiceItem;
import com.facturadian.domain.model.SalesSummary;
import com.facturadian.application.port.out.PdfGeneratorPort;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.enterprise.context.ApplicationScoped;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Adaptador de salida: implementa el puerto PdfGeneratorPort con la librería OpenPDF. */
@ApplicationScoped
public class PdfGeneratorAdapter implements PdfGeneratorPort {

    private static final Color PRIMARY = new Color(13, 110, 253);
    private static final Color LIGHT = new Color(233, 236, 239);
    private static final Font TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.NORMAL, PRIMARY);
    private static final Font SUBTITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font SMALL = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8);
    private static final Font HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.NORMAL, Color.WHITE);

    @Override
    public byte[] generateInvoicePdf(Invoice invoice) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 40, 40);
            PdfWriter.getInstance(document, out);
            document.open();

            Paragraph title = new Paragraph("FACTURA ELECTRÓNICA DE VENTA", TITLE);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            Paragraph number = new Paragraph("N° " + invoice.getInvoiceNumber(), SUBTITLE);
            number.setAlignment(Element.ALIGN_CENTER);
            number.setSpacingAfter(15);
            document.add(number);

            PdfPTable info = new PdfPTable(2);
            info.setWidthPercentage(100);
            info.setWidths(new float[]{1.5f, 4f});
            addInfoRow(info, "Fecha de emisión:", String.valueOf(invoice.getIssueDate()));
            addInfoRow(info, "Estado:", invoice.getStatus().getLabel());
            addInfoRow(info, "Cajero:", invoice.getIssuedByName());
            addInfoRow(info, "Cliente:", invoice.getClient().getName());
            addInfoRow(info, "NIT:", invoice.getClient().getNit());
            addInfoRow(info, "Correo:", invoice.getClient().getEmail() == null ? "-" : invoice.getClient().getEmail());
            addInfoRow(info, "Agente retenedor:", invoice.getClient().isWithholdingAgent() ? "Sí" : "No");
            info.setSpacingAfter(15);
            document.add(info);

            PdfPTable items = new PdfPTable(4);
            items.setWidthPercentage(100);
            items.setWidths(new float[]{5f, 1.5f, 2.5f, 2.5f});
            items.addCell(headerCell("Producto"));
            items.addCell(headerCell("Cant."));
            items.addCell(headerCell("Precio unit."));
            items.addCell(headerCell("Subtotal"));
            for (InvoiceItem item : invoice.getItems()) {
                items.addCell(cell(item.getProductName(), NORMAL, Element.ALIGN_LEFT));
                items.addCell(cell(String.valueOf(item.getQuantity()), NORMAL, Element.ALIGN_CENTER));
                items.addCell(cell(money(item.getUnitPrice()), NORMAL, Element.ALIGN_RIGHT));
                items.addCell(cell(money(item.getSubtotal()), NORMAL, Element.ALIGN_RIGHT));
            }
            items.setSpacingAfter(15);
            document.add(items);

            PdfPTable totals = new PdfPTable(2);
            totals.setWidthPercentage(50);
            totals.setHorizontalAlignment(Element.ALIGN_RIGHT);
            addTotalRow(totals, "Subtotal", money(invoice.getSubtotal()), false);
            addTotalRow(totals, "IVA (19%)", money(invoice.getIva()), false);
            addTotalRow(totals, "ReteIVA (15% del IVA)", "- " + money(invoice.getReteIva()), false);
            addTotalRow(totals, "TOTAL A PAGAR", money(invoice.getTotal()), true);
            document.add(totals);

            Paragraph footer = new Paragraph("Documento generado por FacturaDIAN.", SMALL);
            footer.setSpacingBefore(25);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new BusinessException("No se pudo generar el PDF de la factura: " + e.getMessage());
        }
    }

    @Override
    public byte[] generateSummaryPdf(SalesSummary summary) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 40, 40);
            PdfWriter.getInstance(document, out);
            document.open();

            Paragraph title = new Paragraph("RESUMEN DE VENTAS", TITLE);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            Paragraph period = new Paragraph("Periodo: " + summary.getFrom() + " al " + summary.getTo(), SUBTITLE);
            period.setAlignment(Element.ALIGN_CENTER);
            period.setSpacingAfter(15);
            document.add(period);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 2f, 4f, 2f, 2.7f});
            table.addCell(headerCell("Número"));
            table.addCell(headerCell("Fecha"));
            table.addCell(headerCell("Cliente"));
            table.addCell(headerCell("Estado"));
            table.addCell(headerCell("Total"));
            for (Invoice invoice : summary.getInvoices()) {
                table.addCell(cell(invoice.getInvoiceNumber(), NORMAL, Element.ALIGN_LEFT));
                table.addCell(cell(String.valueOf(invoice.getIssueDate()), NORMAL, Element.ALIGN_CENTER));
                table.addCell(cell(invoice.getClient().getName(), NORMAL, Element.ALIGN_LEFT));
                table.addCell(cell(invoice.getStatus().getLabel(), NORMAL, Element.ALIGN_CENTER));
                table.addCell(cell(money(invoice.getTotal()), NORMAL, Element.ALIGN_RIGHT));
            }
            if (summary.getInvoices().isEmpty()) {
                PdfPCell empty = cell("No hay facturas en este periodo.", NORMAL, Element.ALIGN_CENTER);
                empty.setColspan(5);
                table.addCell(empty);
            }
            table.setSpacingAfter(15);
            document.add(table);

            PdfPTable totals = new PdfPTable(2);
            totals.setWidthPercentage(55);
            totals.setHorizontalAlignment(Element.ALIGN_RIGHT);
            addTotalRow(totals, "Facturas válidas (sin anuladas)", String.valueOf(summary.getValidCount()), false);
            addTotalRow(totals, "Subtotal", money(summary.getSubtotal()), false);
            addTotalRow(totals, "IVA", money(summary.getIva()), false);
            addTotalRow(totals, "ReteIVA", "- " + money(summary.getReteIva()), false);
            addTotalRow(totals, "Total pagado", money(summary.getTotalPaid()), false);
            addTotalRow(totals, "Total pendiente", money(summary.getTotalPending()), false);
            addTotalRow(totals, "TOTAL FACTURADO", money(summary.getTotal()), true);
            document.add(totals);

            Paragraph footer = new Paragraph("Las facturas anuladas se listan pero no suman en los totales. Generado por FacturaDIAN.", SMALL);
            footer.setSpacingBefore(25);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new BusinessException("No se pudo generar el PDF del resumen: " + e.getMessage());
        }
    }

    private PdfPCell cell(String text, Font font, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text == null ? "" : text, font));
        c.setHorizontalAlignment(align);
        c.setPadding(5);
        return c;
    }

    private PdfPCell headerCell(String text) {
        PdfPCell c = cell(text, HEADER, Element.ALIGN_CENTER);
        c.setBackgroundColor(PRIMARY);
        return c;
    }

    private void addInfoRow(PdfPTable table, String label, String value) {
        PdfPCell l = cell(label, BOLD, Element.ALIGN_LEFT);
        l.setBorder(Rectangle.NO_BORDER);
        PdfPCell v = cell(value, NORMAL, Element.ALIGN_LEFT);
        v.setBorder(Rectangle.NO_BORDER);
        table.addCell(l);
        table.addCell(v);
    }

    private void addTotalRow(PdfPTable table, String label, String value, boolean highlight) {
        Font font = highlight ? BOLD : NORMAL;
        PdfPCell l = cell(label, font, Element.ALIGN_LEFT);
        PdfPCell v = cell(value, font, Element.ALIGN_RIGHT);
        if (highlight) {
            l.setBackgroundColor(LIGHT);
            v.setBackgroundColor(LIGHT);
        }
        table.addCell(l);
        table.addCell(v);
    }

    private String money(BigDecimal value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("es-CO"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        return "$ " + new DecimalFormat("#,##0.00", symbols).format(value == null ? BigDecimal.ZERO : value);
    }
}