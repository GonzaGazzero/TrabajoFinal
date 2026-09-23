package com.padelconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inscripciones", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"partido_id", "jugador_id"})
})
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "partido_id", nullable = false)
    private Partido partido;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "jugador_id", nullable = false)
    private Usuario jugador;

    @Column(nullable = false)
    private LocalDateTime fechaInscripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoInscripcion estado;

    @Column(length = 300)
    private String mensaje;

    public Inscripcion(Partido partido, Usuario jugador, EstadoInscripcion estado) {
        this.partido = partido;
        this.jugador = jugador;
        this.fechaInscripcion = LocalDateTime.now();
        this.estado = estado;
    }
}
