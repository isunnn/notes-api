package com.notesapi.devops.controller;

import com.notesapi.devops.dto.CrearNotaRequest;
import com.notesapi.devops.dto.NotaResponse;
import com.notesapi.devops.service.NotaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notas")
public class NotaController {

    private final NotaService notaService;

    public NotaController(NotaService notaService) {
        this.notaService = notaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotaResponse crear(@Valid @RequestBody CrearNotaRequest notaRequest) {
        return notaService.crear(notaRequest);
    }

    @GetMapping 
    public List<NotaResponse> listar() {
    return notaService.listar();
    }
}