package com.padelconnect.repository;

import com.padelconnect.entity.Partido;
import com.padelconnect.entity.Usuario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartidoRepository extends JpaRepository<Partido, Long> {
    List<Partido> findByOrganizador(Usuario organizador);
    List<Partido> findByZonaAndFecha(String zona, String fecha);
    List<Partido> findByZona(String zona);
    List<Partido> findByFecha(String fecha);

    /**
     * Bloquea la fila del partido (SELECT ... FOR UPDATE) hasta que termine la transacción.
     * Se usa donde se cuentan cupos y se decide si entra una nueva inscripción, para que dos
     * solicitudes concurrentes por el último cupo no lean el mismo conteo "viejo" a la vez.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Partido p WHERE p.id = :id")
    Optional<Partido> findByIdForUpdate(@Param("id") Long id);
}
