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
    List<Inscripcion> findByJugador(Usuario jugador);
    long countByPartidoAndEstado(Partido partido, EstadoInscripcion estado);
    Optional<Inscripcion> findByPartidoAndJugador(Partido partido, Usuario jugador);
}
