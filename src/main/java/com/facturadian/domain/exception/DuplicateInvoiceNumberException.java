package com.facturadian.domain.exception;

/** Dos cajeros facturaron a la vez y obtuvieron el mismo número: la base rechaza al segundo. */
public class DuplicateInvoiceNumberException extends BusinessException {
    public DuplicateInvoiceNumberException() {
        super("El número de factura ya existe.");
    }
}