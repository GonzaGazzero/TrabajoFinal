package com.padelconnect.repository;

import com.padelconnect.entity.EstadoInscripcion;
import com.padelconnect.entity.Inscripcion;
import com.padelconnect.entity.Partido;
import com.padelconnect.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {
    List<Inscripcion> findByPartido(Partido partido);
    List<Inscripcion> findByPartidoAndEstado(Partido partido, EstadoInscripcion estado);
    List<Inscripcion> findByJugador(Usuario jugador);
    List<Inscripcion> findByJugadorAndEstado(Usuario jugador, EstadoInscripcion estado);
    long countByPartido(Partido partido);
    long countByPartidoAndEstado(Partido partido, EstadoInscripcion estado);
    boolean existsByPartidoAndJugador(Partido partido, Usuario jugador);
    boolean existsByPartidoAndJugadorAndEstadoIn(Partido partido, Usuario jugador, List<EstadoInscripcion> estados);
    Optional<Inscripcion> findByPartidoAndJugador(Partido partido, Usuario jugador);
    void deleteByPartidoAndJugador(Partido partido, Usuario jugador);
}
