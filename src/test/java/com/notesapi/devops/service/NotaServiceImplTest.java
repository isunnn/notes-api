package com.notesapi.devops.service;

import com.notesapi.devops.dto.CrearNotaRequest;
import com.notesapi.devops.dto.NotaResponse;
import com.notesapi.devops.entity.Nota;
import com.notesapi.devops.repository.NotaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotaServiceImplTest {

    @Test
    void crearTest() {
        NotaRepository repo = mock(NotaRepository.class);
        NotaServiceImpl service = new NotaServiceImpl(repo);

        CrearNotaRequest req = new CrearNotaRequest("Mi nota");

        Nota saved = new Nota("Mi nota");

        when(repo.save(any(Nota.class))).thenReturn(saved);

        NotaResponse res = service.crear(req);

        assertNotNull(res);
        assertEquals("Mi nota", res.nombreNota());

        ArgumentCaptor<Nota> captor = ArgumentCaptor.forClass(Nota.class);
        verify(repo, times(1)).save(captor.capture());
        assertEquals("Mi nota", captor.getValue().getNombreNota());
    }

    @Test
    void listarTest() {
        NotaRepository repo = mock(NotaRepository.class);
        NotaServiceImpl service = new NotaServiceImpl(repo);

        Nota n1 = new Nota("A");
        Nota n2 = new Nota("B");

        when(repo.findAll(Sort.by(Sort.Direction.DESC, "fechaCreacion")))
                .thenReturn(List.of(n2, n1));

        List<NotaResponse> out = service.listar();

        assertEquals(2, out.size());
        assertEquals("B", out.get(0).nombreNota());
        assertEquals("A", out.get(1).nombreNota());

        verify(repo).findAll(Sort.by(Sort.Direction.DESC, "fechaCreacion"));
        verifyNoMoreInteractions(repo);
    }
}
