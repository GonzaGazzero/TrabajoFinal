package com.padelconnect.dto;

import java.util.List;

public class MisPartidosDTO {

    private List<PartidoResponseDTO> creados;
    private List<PartidoResponseDTO> unidos;

    public MisPartidosDTO() {
    }

    public MisPartidosDTO(List<PartidoResponseDTO> creados, List<PartidoResponseDTO> unidos) {
        this.creados = creados;
        this.unidos = unidos;
    }

    public List<PartidoResponseDTO> getCreados() {
        return creados;
    }

    public void setCreados(List<PartidoResponseDTO> creados) {
        this.creados = creados;
    }

    public List<PartidoResponseDTO> getUnidos() {
        return unidos;
    }

    public void setUnidos(List<PartidoResponseDTO> unidos) {
        this.unidos = unidos;
    }
}
