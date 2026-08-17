package com.padelconnect.dto;

import com.padelconnect.entity.EstadoInscripcion;
import com.padelconnect.entity.Inscripcion;

public class InscripcionDTO {

    private Long id;
    private UserDto jugador;
    private EstadoInscripcion estado;
    private String fechaInscripcion;
    private String mensaje;

    public InscripcionDTO() {
    }

    public InscripcionDTO(Long id, UserDto jugador, EstadoInscripcion estado, String fechaInscripcion, String mensaje) {
        this.id = id;
        this.jugador = jugador;
        this.estado = estado;
        this.fechaInscripcion = fechaInscripcion;
        this.mensaje = mensaje;
    }

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserDto getJugador() {
        return jugador;
    }

    public void setJugador(UserDto jugador) {
        this.jugador = jugador;
    }

    public EstadoInscripcion getEstado() {
        return estado;
    }

    public void setEstado(EstadoInscripcion estado) {
        this.estado = estado;
    }

    public String getFechaInscripcion() {
        return fechaInscripcion;
    }

    public void setFechaInscripcion(String fechaInscripcion) {
        this.fechaInscripcion = fechaInscripcion;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
