package com.padelconnect.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@Table(name = "partidos")
public class Partido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{validation.fecha.required}")
    @Column(nullable = false)
    private String fecha;

    @NotBlank(message = "{validation.hora.required}")
    @Column(nullable = false)
    private String hora;

    @NotBlank(message = "{validation.cancha.required}")
    @Column(nullable = false)
    private String cancha;

    @Column
    private String direccion;

    @NotBlank(message = "{validation.zona.required}")
    @Column(nullable = false)
    private String zona;

    @Column
    private Double latitud;

    @Column
    private Double longitud;

    @NotBlank(message = "{validation.nivel.required}")
    @Column(nullable = false)
    private String nivel;

    @NotNull(message = "{validation.cupos.required}")
    @Min(value = 1, message = "{validation.cupos.min}")
    @Column(nullable = false)
    private Integer cuposTotales;

    @Column
    private String precioPersona;

    @Column
    private String tipoCancha;

    @Column
    private String genero;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organizador_id", nullable = false)
    private Usuario organizador;

    public Partido() {
    }

    public Partido(Long id, String fecha, String hora, String cancha, String direccion, String zona,
                   Double latitud, Double longitud, String nivel, Integer cuposTotales,
                   String precioPersona, String tipoCancha, String genero, Usuario organizador) {
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
        this.precioPersona = precioPersona;
        this.tipoCancha = tipoCancha;
        this.genero = genero;
        this.organizador = organizador;
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

    public Usuario getOrganizador() {
        return organizador;
    }

    public void setOrganizador(Usuario organizador) {
        this.organizador = organizador;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Partido partido = (Partido) o;
        return Objects.equals(id, partido.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
