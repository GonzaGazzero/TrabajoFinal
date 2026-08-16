package com.padelconnect.dto;

import com.padelconnect.entity.Usuario;

public class JugadorDTO {

    private Long id;
    private String nombre;
    private String iniciales;
    private String rol;

    public JugadorDTO() {
    }

    public JugadorDTO(Long id, String nombre, String iniciales, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.iniciales = iniciales;
        this.rol = rol;
    }

    public static JugadorDTO fromEntity(Usuario usuario, String rol) {
        if (usuario == null) return null;
        return new JugadorDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getAvatarIniciales(),
                rol
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getIniciales() {
        return iniciales;
    }

    public void setIniciales(String iniciales) {
        this.iniciales = iniciales;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
