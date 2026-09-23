package com.padelconnect.dto;

import java.util.List;
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
public class PartidoResponseDTO {

    private Long id;
    private String fecha;
    private String hora;
    private String cancha;
    private String direccion;
    private String zona;
    private Double latitud;
    private Double longitud;
    private String nivel;
    private Integer cuposTotales;
    private Integer cuposDisponibles;
    private Integer jugadoresFaltantes;
    private Double distanciaKm;
    private String precioPersona;
    private String tipoCancha;
    private String genero;
    private UserDto organizador;
    private List<JugadorDTO> jugadores;
    private List<InscripcionDTO> solicitudesPendientes;
    private String estadoInscripcionUsuario;
}
