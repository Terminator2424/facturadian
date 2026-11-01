package com.facturadian.domain.exception;

/** Error de regla de negocio. Su mensaje se muestra al usuario en la vista. */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}