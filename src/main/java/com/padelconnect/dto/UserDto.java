package com.padelconnect.dto;

import com.padelconnect.entity.Usuario;

public class UserDto {

    private Long id;
    private String nombre;
    private String email;
    private String telefono;
    private String nivel;
    private String avatarIniciales;

    public UserDto() {
    }

    public UserDto(Long id, String nombre, String email, String telefono, String nivel, String avatarIniciales) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.nivel = nivel;
        this.avatarIniciales = avatarIniciales;
    }

    public static UserDto fromEntity(Usuario usuario) {
        if (usuario == null) return null;
        return new UserDto(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getEmail(),
            usuario.getTelefono(),
            usuario.getNivel(),
            usuario.getAvatarIniciales()
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public String getAvatarIniciales() {
        return avatarIniciales;
    }

    public void setAvatarIniciales(String avatarIniciales) {
        this.avatarIniciales = avatarIniciales;
    }
}
