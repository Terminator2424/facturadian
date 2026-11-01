package com.facturadian.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "clients")
public class ClientEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String nit;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 120)
    private String email;

    @Column(name = "withholding_agent", nullable = false)
    private boolean withholdingAgent;

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