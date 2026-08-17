package com.padelconnect.dto;

import jakarta.validation.constraints.Size;

public class UnirseRequestDTO {

    @Size(max = 300, message = "{validation.mensaje.size}")
    private String mensaje;

    public UnirseRequestDTO() {
    }

    public UnirseRequestDTO(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
