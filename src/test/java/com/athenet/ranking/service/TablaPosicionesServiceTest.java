package com.athenet.ranking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.athenet.ranking.mapper.FilaTablaMapper;
import com.athenet.ranking.model.FilaTabla;
import com.athenet.ranking.model.ResultadoProcesado;
import com.athenet.ranking.repository.FilaTablaRepository;
import com.athenet.ranking.repository.ResultadoProcesadoRepository;

/**
 * Prueba el calculo de estadisticas y el orden de la tabla de posiciones
 * a partir de resultados procesados, sin tocar una base de datos real.
 */
@ExtendWith(MockitoExtension.class)
class TablaPosicionesServiceTest {

    @Mock
    private ResultadoProcesadoRepository resultadoProcesadoRepository;
    @Mock
    private FilaTablaRepository filaTablaRepository;
    @Mock
    private FilaTablaMapper filaTablaMapper;

    @InjectMocks
    private TablaPosicionesService service;

    @Test
    void recalcular_calculaEstadisticasYOrdenaPorPtsDiferenciaYFavor() {
        Long competenciaId = 100L;

        // A (10) le gana 2-1 a B (20) de local
        ResultadoProcesado r1 = ResultadoProcesado.builder()
                .partidoId(1L).competenciaId(competenciaId).deporteId(1L).version(1)
                .equipoLocalId(10L).equipoVisitaId(20L)
                .institucionLocalId(1L).institucionVisitaId(2L)
                .puntajeLocal(2).puntajeVisita(1)
                .puntosVictoria(3).puntosEmpate(1).puntosDerrota(0)
                .build();

        // C (30) empata 0-0 de local contra A (10)
        ResultadoProcesado r2 = ResultadoProcesado.builder()
                .partidoId(2L).competenciaId(competenciaId).deporteId(1L).version(1)
                .equipoLocalId(30L).equipoVisitaId(10L)
                .institucionLocalId(3L).institucionVisitaId(1L)
                .puntajeLocal(0).puntajeVisita(0)
                .puntosVictoria(3).puntosEmpate(1).puntosDerrota(0)
                .build();

        when(resultadoProcesadoRepository.findByCompetenciaId(competenciaId)).thenReturn(List.of(r1, r2));
        when(filaTablaRepository.findByCompetenciaIdAndEquipoId(any(), any())).thenReturn(Optional.empty());

        service.recalcular(competenciaId);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<FilaTabla>> captor = ArgumentCaptor.forClass(List.class);
        verify(filaTablaRepository).saveAll(captor.capture());
        List<FilaTabla> filas = captor.getValue();

        FilaTabla equipoA = buscar(filas, 10L);
        assertThat(equipoA.getPj()).isEqualTo(2);
        assertThat(equipoA.getPg()).isEqualTo(1);
        assertThat(equipoA.getPe()).isEqualTo(1);
        assertThat(equipoA.getPp()).isEqualTo(0);
        assertThat(equipoA.getPuntosFavor()).isEqualTo(2);
        assertThat(equipoA.getPuntosContra()).isEqualTo(1);
        assertThat(equipoA.getDiferencia()).isEqualTo(1);
        assertThat(equipoA.getPts()).isEqualTo(4);
        assertThat(equipoA.getPosicion()).isEqualTo(1);

        FilaTabla equipoC = buscar(filas, 30L);
        assertThat(equipoC.getPts()).isEqualTo(1);
        assertThat(equipoC.getPosicion()).isEqualTo(2);

        FilaTabla equipoB = buscar(filas, 20L);
        assertThat(equipoB.getPts()).isEqualTo(0);
        assertThat(equipoB.getPosicion()).isEqualTo(3);
    }

    private FilaTabla buscar(List<FilaTabla> filas, Long equipoId) {
        return filas.stream().filter(f -> f.getEquipoId().equals(equipoId)).findFirst()
                .orElseThrow(() -> new AssertionError("No se encontro fila para equipo " + equipoId));
    }
}
