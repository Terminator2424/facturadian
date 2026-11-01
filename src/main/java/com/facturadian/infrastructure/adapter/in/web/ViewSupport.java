package com.facturadian.infrastructure.adapter.in.web;

import com.facturadian.domain.exception.BusinessException;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Utilidades comunes de las vistas: alertas, redirecciones y lectura de fechas. */
public final class ViewSupport {
    private ViewSupport() {}

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /** Agrega a la plantilla las alertas de éxito y/o error, si existen. */
    public static TemplateInstance withMessages(TemplateInstance template, String success, String error) {
        if (!isBlank(success)) {
            template.data("success", success);
        }
        if (!isBlank(error)) {
            template.data("error", error);
        }
        return template;
    }

    /** Redirección 303 (POST/Redirect/GET) con el mensaje de éxito o error en la URL. */
    public static Response redirect(String path, String success, String error) {
        StringBuilder url = new StringBuilder(path);
        String separator = "?";
        if (!isBlank(success)) {
            url.append(separator).append("success=").append(encode(success));
            separator = "&";
        }
        if (!isBlank(error)) {
            url.append(separator).append("error=").append(encode(error));
        }
        return Response.seeOther(URI.create(url.toString())).build();
    }

    public static LocalDate parseDate(String raw) {
        if (isBlank(raw)) {
            return null;
        }
        try {
            return LocalDate.parse(raw.trim());
        } catch (DateTimeParseException e) {
            throw new BusinessException("La fecha \"" + raw + "\" no es válida.");
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}