package com.padelconnect.dto;

import com.padelconnect.entity.EstadoInscripcion;
import com.padelconnect.entity.Inscripcion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InscripcionDTO {

    private Long id;
    private UserDto jugador;
    private EstadoInscripcion estado;
    private String fechaInscripcion;
    private String mensaje;

    public static InscripcionDTO fromEntity(Inscripcion inscripcion) {
        if (inscripcion == null) return null;
        return new InscripcionDTO(
                inscripcion.getId(),
                UserDto.fromEntity(inscripcion.getJugador()),
                inscripcion.getEstado(),
                inscripcion.getFechaInscripcion() != null ? inscripcion.getFechaInscripcion().toString() : null,
                inscripcion.getMensaje()
        );
    }
}
