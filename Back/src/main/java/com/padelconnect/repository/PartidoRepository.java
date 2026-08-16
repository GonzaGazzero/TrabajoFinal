package com.padelconnect.repository;

import com.padelconnect.entity.Partido;
import com.padelconnect.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartidoRepository extends JpaRepository<Partido, Long> {
    List<Partido> findByOrganizador(Usuario organizador);
    List<Partido> findByZonaAndFecha(String zona, String fecha);
    List<Partido> findByZona(String zona);
    List<Partido> findByFecha(String fecha);
}
