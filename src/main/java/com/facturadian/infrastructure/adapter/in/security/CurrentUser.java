package com.facturadian.infrastructure.adapter.in.security;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/** Expone el nombre del cajero a las plantillas: {inject:me.shortName}. */
@Named("me")
@RequestScoped
public class CurrentUser {

    @Inject
    SecurityIdentity identity;

    public String getFullName() {
        if (identity == null || identity.isAnonymous()) {
            return "";
        }
        String name = identity.getAttribute("fullName");
        return name != null ? name : identity.getPrincipal().getName();
    }

    public boolean isAdmin() {
        return identity != null && identity.hasRole("admin");
    }

    public boolean isCashier() {
        return identity != null && identity.hasRole("cashier");
    }

    public String getRoleLabel() {
        return isAdmin() ? "ADMIN" : "CAJERO";
    }

    public String getShortName() {
        String full = getFullName().trim();
        if (full.isEmpty()) {
            return "INVITADO";
        }
        return full.split("\\s+")[0].toUpperCase();
    }
}