package com.facturadian.domain.model;

/** Rol del usuario: el administrador es el dueño de la tienda (solo hay uno); los demás son cajeros. */
public enum Role {
    ADMIN("admin"),
    CASHIER("cashier");

    private final String key;

    Role(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}