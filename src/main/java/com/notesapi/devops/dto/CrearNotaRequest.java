package com.notesapi.devops.dto;

import jakarta.validation.constraints.NotBlank;

public record CrearNotaRequest(
    @NotBlank(message = "nombreNota es obligatorio")
    String nombreNota
    )
{}
