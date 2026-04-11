package com.notesapi.devops.service;

import com.notesapi.devops.dto.CrearNotaRequest;
import com.notesapi.devops.dto.NotaResponse;

public interface NotaService {
    NotaResponse crear(CrearNotaRequest notaRequest);
}