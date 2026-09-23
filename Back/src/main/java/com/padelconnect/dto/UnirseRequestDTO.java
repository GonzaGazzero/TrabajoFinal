package com.padelconnect.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnirseRequestDTO {

    @Size(max = 300, message = "{validation.mensaje.size}")
    private String mensaje;
}
