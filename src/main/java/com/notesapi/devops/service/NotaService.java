package com.notesapi.devops.service;

import com.notesapi.devops.dto.CrearNotaRequest;
import com.notesapi.devops.dto.NotaResponse;
import java.util.List;

public interface NotaService {
    NotaResponse crear(CrearNotaRequest notaRequest);
    List<NotaResponse> listar();
}