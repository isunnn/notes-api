package com.notesapi.devops.entity;

import java.time.Instant;
import jakarta.persistence.*;

@Entity
@Table(name = "notas")
public class Nota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombreNota;

    @Column(nullable = false, updatable = false)
    private Instant fechaCreacion = Instant.now();

    protected Nota() {}

    public Nota(String nombreNota) {
        this.nombreNota = nombreNota;
    }

    public Long getId() { return id; }
    public String getNombreNota() { return nombreNota; }
    public Instant getFechaCreacion() { return fechaCreacion; }

    public void setNombreNota(String nombreNota) { this.nombreNota = nombreNota; }
}