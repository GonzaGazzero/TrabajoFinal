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
public class UserDto {

    private Long id;
    private String nombre;
    private String email;
    private String telefono;
    private String nivel;
    private String avatarIniciales;

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
}
