package com.padelconnect.dto;

import java.util.List;

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

    public PartidoResponseDTO() {
    }

    public PartidoResponseDTO(Long id, String fecha, String hora, String cancha, String direccion, String zona,
                              Double latitud, Double longitud, String nivel, Integer cuposTotales,
                              Integer cuposDisponibles, Integer jugadoresFaltantes, Double distanciaKm, String precioPersona,
                              String tipoCancha, String genero, UserDto organizador, List<JugadorDTO> jugadores,
                              List<InscripcionDTO> solicitudesPendientes, String estadoInscripcionUsuario) {
        this.id = id;
        this.fecha = fecha;
        this.hora = hora;
        this.cancha = cancha;
        this.direccion = direccion;
        this.zona = zona;
        this.latitud = latitud;
        this.longitud = longitud;
        this.nivel = nivel;
        this.cuposTotales = cuposTotales;
        this.cuposDisponibles = cuposDisponibles;
        this.jugadoresFaltantes = jugadoresFaltantes;
        this.distanciaKm = distanciaKm;
        this.precioPersona = precioPersona;
        this.tipoCancha = tipoCancha;
        this.genero = genero;
        this.organizador = organizador;
        this.jugadores = jugadores;
        this.solicitudesPendientes = solicitudesPendientes;
        this.estadoInscripcionUsuario = estadoInscripcionUsuario;
    }

    public PartidoResponseDTO(Long id, String fecha, String hora, String cancha, String direccion, String zona,
                              Double latitud, Double longitud, String nivel, Integer cuposTotales,
                              Integer cuposDisponibles, Double distanciaKm, String precioPersona,
                              String tipoCancha, UserDto organizador, List<JugadorDTO> jugadores) {
        this(id, fecha, hora, cancha, direccion, zona, latitud, longitud, nivel, cuposTotales,
             cuposDisponibles, cuposDisponibles, distanciaKm, precioPersona, tipoCancha, null, organizador,
             jugadores, null, null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getCancha() {
        return cancha;
    }

    public void setCancha(String cancha) {
        this.cancha = cancha;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getZona() {
        return zona;
    }

    public void setZona(String zona) {
        this.zona = zona;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public Integer getCuposTotales() {
        return cuposTotales;
    }

    public void setCuposTotales(Integer cuposTotales) {
        this.cuposTotales = cuposTotales;
    }

    public Integer getCuposDisponibles() {
        return cuposDisponibles;
    }

    public void setCuposDisponibles(Integer cuposDisponibles) {
        this.cuposDisponibles = cuposDisponibles;
    }

    public Double getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(Double distanciaKm) {
        this.distanciaKm = distanciaKm;
    }

    public String getPrecioPersona() {
        return precioPersona;
    }

    public void setPrecioPersona(String precioPersona) {
        this.precioPersona = precioPersona;
    }

    public String getTipoCancha() {
        return tipoCancha;
    }

    public void setTipoCancha(String tipoCancha) {
        this.tipoCancha = tipoCancha;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public UserDto getOrganizador() {
        return organizador;
    }

    public void setOrganizador(UserDto organizador) {
        this.organizador = organizador;
    }

    public List<JugadorDTO> getJugadores() {
        return jugadores;
    }

    public void setJugadores(List<JugadorDTO> jugadores) {
        this.jugadores = jugadores;
    }

    public Integer getJugadoresFaltantes() {
        return jugadoresFaltantes;
    }

    public void setJugadoresFaltantes(Integer jugadoresFaltantes) {
        this.jugadoresFaltantes = jugadoresFaltantes;
    }

    public List<InscripcionDTO> getSolicitudesPendientes() {
        return solicitudesPendientes;
    }

    public void setSolicitudesPendientes(List<InscripcionDTO> solicitudesPendientes) {
        this.solicitudesPendientes = solicitudesPendientes;
    }

    public String getEstadoInscripcionUsuario() {
        return estadoInscripcionUsuario;
    }

    public void setEstadoInscripcionUsuario(String estadoInscripcionUsuario) {
        this.estadoInscripcionUsuario = estadoInscripcionUsuario;
    }
}
