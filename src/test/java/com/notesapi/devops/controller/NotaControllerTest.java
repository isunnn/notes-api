package com.notesapi.devops.controller;

import com.notesapi.devops.dto.CrearNotaRequest;
import com.notesapi.devops.dto.NotaResponse;
import com.notesapi.devops.service.NotaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotaController.class)
class NotaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotaService notaService;

    @Test
    void post_crear_201() throws Exception {
        NotaResponse response = new NotaResponse(
                1L,
                "Mi nota",
                Instant.parse("2026-01-01T00:00:00Z")
        );

        when(notaService.crear(any(CrearNotaRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/notas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nombreNota": "Mi nota" }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombreNota").value("Mi nota"))
                .andExpect(jsonPath("$.fechaCreacion").value("2026-01-01T00:00:00Z"));

        verify(notaService, times(1)).crear(any(CrearNotaRequest.class));
        verifyNoMoreInteractions(notaService);
    }

    @Test
    void get_listar_200() throws Exception {
        List<NotaResponse> data = List.of(
                new NotaResponse(2L, "B", Instant.parse("2026-01-02T00:00:00Z")),
                new NotaResponse(1L, "A", Instant.parse("2026-01-01T00:00:00Z"))
        );

        when(notaService.listar()).thenReturn(data);

        mockMvc.perform(get("/api/notas")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].nombreNota").value("B"))
                .andExpect(jsonPath("$[1].id").value(1))
                .andExpect(jsonPath("$[1].nombreNota").value("A"));

        verify(notaService, times(1)).listar();
        verifyNoMoreInteractions(notaService);
    }

    @Test
    void post_crear_400() throws Exception {
        mockMvc.perform(post("/api/notas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nombreNota": "" }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(notaService);
    }
}