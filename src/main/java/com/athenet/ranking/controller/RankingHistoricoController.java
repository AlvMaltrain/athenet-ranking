package com.athenet.ranking.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.athenet.ranking.dto.response.RankingHistoricoResponse;
import com.athenet.ranking.service.RankingHistoricoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/deportes")
@RequiredArgsConstructor
public class RankingHistoricoController {

    private final RankingHistoricoService rankingHistoricoService;

    @GetMapping("/{deporteId}/ranking-historico")
    public ResponseEntity<List<RankingHistoricoResponse>> obtenerRanking(@PathVariable Long deporteId) {
        return ResponseEntity.ok(rankingHistoricoService.obtenerRanking(deporteId));
    }
}
