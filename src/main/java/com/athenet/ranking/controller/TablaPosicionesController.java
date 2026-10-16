package com.athenet.ranking.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.athenet.ranking.dto.response.FilaTablaResponse;
import com.athenet.ranking.service.TablaPosicionesService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/competencias")
@RequiredArgsConstructor
public class TablaPosicionesController {

    private final TablaPosicionesService tablaPosicionesService;

    @GetMapping("/{competenciaId}/tabla")
    public ResponseEntity<List<FilaTablaResponse>> obtenerTabla(@PathVariable Long competenciaId) {
        return ResponseEntity.ok(tablaPosicionesService.obtenerTabla(competenciaId));
    }
}
