package com.athenet.ranking.mapper;

import org.springframework.stereotype.Component;

import com.athenet.ranking.dto.response.FilaTablaResponse;
import com.athenet.ranking.model.FilaTabla;

@Component
public class FilaTablaMapper {

    public FilaTablaResponse toResponse(FilaTabla fila) {
        return new FilaTablaResponse(
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
