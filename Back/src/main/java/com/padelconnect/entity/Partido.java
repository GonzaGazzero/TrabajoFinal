package com.padelconnect.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "partidos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
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
}
