package com.padelconnect.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "{validation.nombre.required}")
    private String nombre;

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.invalid}")
    private String email;

    @NotBlank(message = "{validation.password.required}")
    @Size(min = 6, message = "{validation.password.size}")
    private String password;

    @NotBlank(message = "{validation.confirmPassword.required}")
    private String confirmPassword;

    private String telefono;
    private String nivel;

    public RegisterRequest() {
    }

    public RegisterRequest(String nombre, String email, String password, String confirmPassword, String telefono, String nivel) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.telefono = telefono;
        this.nivel = nivel;
    }

    public RegisterRequest(String nombre, String email, String password, String telefono, String nivel) {
        this(nombre, email, password, password, telefono, nivel);
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getConfirmarPassword() {
        return confirmPassword;
    }

    public void setConfirmarPassword(String confirmarPassword) {
        this.confirmPassword = confirmarPassword;
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
}
