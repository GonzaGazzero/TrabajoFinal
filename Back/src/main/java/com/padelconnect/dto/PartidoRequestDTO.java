package com.padelconnect.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartidoRequestDTO {

    @NotBlank(message = "{validation.fecha.required}")
    private String fecha;

    @NotBlank(message = "{validation.hora.required}")
    private String hora;

    @NotBlank(message = "{validation.cancha.required}")
    private String cancha;

    private String direccion;

    @NotBlank(message = "{validation.zona.required}")
    private String zona;

    private Double latitud;
    private Double longitud;

    @NotBlank(message = "{validation.nivel.required}")
    private String nivel;

    @Min(value = 1, message = "{validation.cupos.min}")
    private Integer cuposTotales;

    private Integer jugadoresFaltantes;

    private String precioPersona;
    private String tipoCancha;
    private String genero;
}
