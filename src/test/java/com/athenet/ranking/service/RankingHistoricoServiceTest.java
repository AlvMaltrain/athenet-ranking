package com.athenet.ranking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
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

import com.athenet.ranking.mapper.RankingHistoricoMapper;
import com.athenet.ranking.model.RankingHistorico;
import com.athenet.ranking.model.ResultadoProcesado;
import com.athenet.ranking.repository.RankingHistoricoRepository;
import com.athenet.ranking.repository.ResultadoProcesadoRepository;

/**
 * Prueba el calculo de estadisticas historicas por equipo y el reordenamiento
 * del ranking completo del deporte, sin tocar una base de datos real.
 */
@ExtendWith(MockitoExtension.class)
class RankingHistoricoServiceTest {

    @Mock
    private ResultadoProcesadoRepository resultadoProcesadoRepository;
    @Mock
    private RankingHistoricoRepository rankingHistoricoRepository;
    @Mock
    private RankingHistoricoMapper rankingHistoricoMapper;

    @InjectMocks
    private RankingHistoricoService service;

    @Test
    void actualizar_recalculaEstadisticasHistoricasDeAmbosEquiposDesdeCero() {
        Long deporteId = 5L;

        // Historial de A (10): le gana 3-1 a B (20) de local, y empata 2-2 de visita contra D (40)
        ResultadoProcesado vsB = ResultadoProcesado.builder()
                .partidoId(1L).competenciaId(1L).deporteId(deporteId).version(1)
                .equipoLocalId(10L).equipoVisitaId(20L)
                .institucionLocalId(1L).institucionVisitaId(2L)
                .puntajeLocal(3).puntajeVisita(1)
                .puntosVictoria(2).puntosEmpate(1).puntosDerrota(0)
                .build();
        ResultadoProcesado vsD = ResultadoProcesado.builder()
                .partidoId(2L).competenciaId(2L).deporteId(deporteId).version(1)
                .equipoLocalId(40L).equipoVisitaId(10L)
                .institucionLocalId(4L).institucionVisitaId(1L)
                .puntajeLocal(2).puntajeVisita(2)
                .puntosVictoria(2).puntosEmpate(1).puntosDerrota(0)
                .build();

        when(resultadoProcesadoRepository.findByDeporteIdAndEquipo(deporteId, 10L)).thenReturn(List.of(vsB, vsD));
        when(resultadoProcesadoRepository.findByDeporteIdAndEquipo(deporteId, 20L)).thenReturn(List.of(vsB));
        when(rankingHistoricoRepository.findByDeporteIdAndEquipoId(any(), any())).thenReturn(Optional.empty());
        when(rankingHistoricoRepository.findByDeporteId(deporteId)).thenReturn(List.of());

        service.actualizar(vsB);

        ArgumentCaptor<RankingHistorico> captor = ArgumentCaptor.forClass(RankingHistorico.class);
        verify(rankingHistoricoRepository, times(2)).save(captor.capture());
        List<RankingHistorico> guardados = captor.getAllValues();

        RankingHistorico equipoA = buscar(guardados, 10L);
        assertThat(equipoA.getPj()).isEqualTo(2);
        assertThat(equipoA.getPg()).isEqualTo(1);
        assertThat(equipoA.getPe()).isEqualTo(1);
        assertThat(equipoA.getPp()).isEqualTo(0);
        assertThat(equipoA.getPuntosFavor()).isEqualTo(5);
        assertThat(equipoA.getPuntosContra()).isEqualTo(3);
        assertThat(equipoA.getDiferencia()).isEqualTo(2);
        assertThat(equipoA.getPts()).isEqualTo(3);

        RankingHistorico equipoB = buscar(guardados, 20L);
        assertThat(equipoB.getPj()).isEqualTo(1);
        assertThat(equipoB.getPp()).isEqualTo(1);
        assertThat(equipoB.getPts()).isEqualTo(0);
    }

    @Test
    void actualizar_reordenaTodoElRankingDelDeporte_noSoloLosDosEquiposDelResultado() {
        Long deporteId = 5L;

        ResultadoProcesado resultado = ResultadoProcesado.builder()
                .partidoId(1L).competenciaId(1L).deporteId(deporteId).version(1)
                .equipoLocalId(10L).equipoVisitaId(20L)
                .institucionLocalId(1L).institucionVisitaId(2L)
                .puntajeLocal(1).puntajeVisita(1)
                .puntosVictoria(3).puntosEmpate(1).puntosDerrota(0)
                .build();

        when(resultadoProcesadoRepository.findByDeporteIdAndEquipo(any(), any())).thenReturn(List.of(resultado));
        when(rankingHistoricoRepository.findByDeporteIdAndEquipoId(any(), any())).thenReturn(Optional.empty());

        // El ranking del deporte ya tenia un tercer equipo (C) que no jugo este partido,
        // y que debe conservar su lugar relativo frente a los que si se recalcularon.
        RankingHistorico equipoC = RankingHistorico.builder()
                .deporteId(deporteId).equipoId(30L).institucionId(3L)
                .pj(5).pts(10).diferencia(4).puntosFavor(8)
                .build();
        RankingHistorico equipoA = RankingHistorico.builder()
                .deporteId(deporteId).equipoId(10L).institucionId(1L)
                .pj(1).pts(1).diferencia(0).puntosFavor(1)
                .build();
        RankingHistorico equipoB = RankingHistorico.builder()
                .deporteId(deporteId).equipoId(20L).institucionId(2L)
                .pj(1).pts(1).diferencia(0).puntosFavor(1)
                .build();
        when(rankingHistoricoRepository.findByDeporteId(deporteId)).thenReturn(List.of(equipoC, equipoA, equipoB));

        service.actualizar(resultado);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RankingHistorico>> captor = ArgumentCaptor.forClass(List.class);
        verify(rankingHistoricoRepository).saveAll(captor.capture());
        List<RankingHistorico> ordenado = captor.getValue();

        assertThat(ordenado.get(0).getEquipoId()).isEqualTo(30L); // C: pts=10, queda primero
        assertThat(ordenado.get(0).getPosicion()).isEqualTo(1);
        assertThat(ordenado.get(1).getPosicion()).isEqualTo(2);
        assertThat(ordenado.get(2).getPosicion()).isEqualTo(3);
    }

    private RankingHistorico buscar(List<RankingHistorico> filas, Long equipoId) {
        return filas.stream().filter(f -> f.getEquipoId().equals(equipoId)).findFirst()
                .orElseThrow(() -> new AssertionError("No se encontro fila para equipo " + equipoId));
    }
}
