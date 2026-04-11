package com.notesapi.devops.service;

import com.notesapi.devops.dto.CrearNotaRequest;
import com.notesapi.devops.dto.NotaResponse;
import com.notesapi.devops.entity.Nota;
import com.notesapi.devops.repository.NotaRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotaServiceImpl implements NotaService {

    private final NotaRepository notaRepository;

    public NotaServiceImpl(NotaRepository notaRepository) {
        this.notaRepository = notaRepository;
    }

    @Override
    public NotaResponse crear(CrearNotaRequest notaRequest) {
        Nota nota = new Nota(notaRequest.nombreNota());
        Nota save = notaRepository.save(nota);

        return new NotaResponse(
                save.getId(),
                save.getNombreNota(),
                save.getFechaCreacion()
        );
    }

    @Override
    public List<NotaResponse> listar() {
        return notaRepository.findAll()
                .stream()
                .map(nota -> new NotaResponse(
                        nota.getId(),
                        nota.getNombreNota(),
                        nota.getFechaCreacion()
                ))
                .toList();
    }
    
}