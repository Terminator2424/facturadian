package com.facturadian.domain.model;

public class Client {
    private Long id;
    private String nit;
    private String name;
    private String email;
    private boolean withholdingAgent; // agente retenedor de IVA

    public Client() {}

    public Client(String nit, String name, String email, boolean withholdingAgent) {
        this.nit = nit;
        this.name = name;
        this.email = email;
        this.withholdingAgent = withholdingAgent;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public boolean isWithholdingAgent() { return withholdingAgent; }
    public void setWithholdingAgent(boolean withholdingAgent) { this.withholdingAgent = withholdingAgent; }
}