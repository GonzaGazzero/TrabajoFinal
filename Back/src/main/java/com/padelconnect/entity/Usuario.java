package com.padelconnect.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{validation.nombre.required}")
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.invalid}")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "{validation.password.required}")
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String telefono;

    @Column(nullable = false)
    private String nivel;

    public String getAvatarIniciales() {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "U";
        }
        String[] partes = nombre.trim().split("\\s+");
        if (partes.length == 1) {
            return partes[0].substring(0, Math.min(2, partes[0].length())).toUpperCase();
        }
        return (partes[0].substring(0, 1) + partes[1].substring(0, 1)).toUpperCase();
    }
}
