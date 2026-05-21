package com.notesapi.devops.dto;

import java.time.Instant;

public record NotaResponse(
        Long id,
        String nombreNota,
        Instant fechaCreacion

)

{
}
