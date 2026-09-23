package com.padelconnect.dto;

import com.padelconnect.entity.Usuario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JugadorDTO {

    private Long id;
    private String nombre;
    private String iniciales;
    private String rol;

    public static JugadorDTO fromEntity(Usuario usuario, String rol) {
        if (usuario == null) return null;
        return new JugadorDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getAvatarIniciales(),
                rol
        );
    }
}
