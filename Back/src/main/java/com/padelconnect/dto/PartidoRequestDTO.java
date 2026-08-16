package com.padelconnect.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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

    public PartidoRequestDTO() {
    }

    public PartidoRequestDTO(String fecha, String hora, String cancha, String direccion, String zona,
                             Double latitud, Double longitud, String nivel, Integer cuposTotales,
                             Integer jugadoresFaltantes, String precioPersona, String tipoCancha) {
        this(fecha, hora, cancha, direccion, zona, latitud, longitud, nivel, cuposTotales,
                jugadoresFaltantes, precioPersona, tipoCancha, null);
    }

    public PartidoRequestDTO(String fecha, String hora, String cancha, String direccion, String zona,
                             Double latitud, Double longitud, String nivel, Integer cuposTotales,
                             Integer jugadoresFaltantes, String precioPersona, String tipoCancha, String genero) {
        this.fecha = fecha;
        this.hora = hora;
        this.cancha = cancha;
        this.direccion = direccion;
        this.zona = zona;
        this.latitud = latitud;
        this.longitud = longitud;
        this.nivel = nivel;
        this.cuposTotales = cuposTotales;
        this.jugadoresFaltantes = jugadoresFaltantes;
        this.precioPersona = precioPersona;
        this.tipoCancha = tipoCancha;
        this.genero = genero;
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

    public Integer getJugadoresFaltantes() {
        return jugadoresFaltantes;
    }

    public void setJugadoresFaltantes(Integer jugadoresFaltantes) {
        this.jugadoresFaltantes = jugadoresFaltantes;
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
}
