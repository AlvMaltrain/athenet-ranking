package com.athenet.ranking.service;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import com.athenet.ranking.dto.message.*;
import com.athenet.ranking.model.ResultadoProcesado;
import com.athenet.ranking.repository.ResultadoProcesadoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class ResultadoProcesadoServiceTest {
    ResultadoRegistradoMessage fixture(int version) throws Exception {
        try (var input = getClass().getResourceAsStream("/contracts/result-v" + version + ".json")) {
            return new ObjectMapper().findAndRegisterModules().readValue(input,ResultadoRegistradoEnvelope.class).data();
        }
    }
    @Test void correctionReplacesSnapshotAndDuplicateOrOldVersionDoesNotRecalculate() throws Exception {
        var repository = mock(ResultadoProcesadoRepository.class);
        var table = mock(TablaPosicionesService.class);
        var ranking = mock(RankingHistoricoService.class);
        var current = new AtomicReference<ResultadoProcesado>();
        when(repository.findById(anyLong())).thenAnswer(call -> Optional.ofNullable(current.get()));
        when(repository.save(any(ResultadoProcesado.class))).thenAnswer(call -> {
            ResultadoProcesado value = call.getArgument(0); current.set(value); return value;
        });
        var service = new ResultadoProcesadoService(repository,table,ranking);
        var initial = fixture(1); var correction = fixture(2);
        service.procesar(initial); service.procesar(initial);
        service.procesar(correction); service.procesar(initial); service.procesar(correction);
        assertThat(current.get().getVersion()).isEqualTo(2);
        assertThat(current.get().getPuntajeLocal()).isEqualTo(correction.homeScore());
        assertThat(current.get().getPuntajeVisita()).isEqualTo(correction.awayScore());
        verify(repository,times(2)).save(any(ResultadoProcesado.class));
        verify(table,times(2)).recalcular(initial.competitionId());
        verify(ranking,times(2)).actualizar(any(ResultadoProcesado.class));
    }
}
