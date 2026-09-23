package com.padelconnect.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MisPartidosDTO {

    private List<PartidoResponseDTO> creados;
    private List<PartidoResponseDTO> unidos;
}
