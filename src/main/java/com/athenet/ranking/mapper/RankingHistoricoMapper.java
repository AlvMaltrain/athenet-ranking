package com.athenet.ranking.mapper;

import org.springframework.stereotype.Component;

import com.athenet.ranking.dto.response.RankingHistoricoResponse;
import com.athenet.ranking.model.RankingHistorico;

@Component
public class RankingHistoricoMapper {

    public RankingHistoricoResponse toResponse(RankingHistorico fila) {
        return new RankingHistoricoResponse(
                fila.getEquipoId(),
                fila.getInstitucionId(),
                fila.getPj(),
                fila.getPg(),
                fila.getPe(),
                fila.getPp(),
                fila.getPuntosFavor(),
                fila.getPuntosContra(),
                fila.getDiferencia(),
                fila.getPts(),
                fila.getPosicion()
        );
    }
}
