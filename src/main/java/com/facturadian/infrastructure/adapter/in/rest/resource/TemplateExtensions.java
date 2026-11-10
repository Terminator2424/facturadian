package com.facturadian.infrastructure.adapter.in.rest.resource;

import io.quarkus.qute.TemplateExtension;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Permite escribir {invoice.total.money} en las plantillas. */
@TemplateExtension
public class TemplateExtensions {

    static String money(BigDecimal value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("es-CO"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        return "$ " + new DecimalFormat("#,##0.00", symbols).format(value == null ? BigDecimal.ZERO : value);
    }
}