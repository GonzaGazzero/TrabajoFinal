package com.padelconnect;

import com.padelconnect.dto.PartidoRequestDTO;
import com.padelconnect.dto.PartidoResponseDTO;
import com.padelconnect.entity.EstadoInscripcion;
import com.padelconnect.entity.Usuario;
import com.padelconnect.exception.CupoLlenoException;
import com.padelconnect.repository.InscripcionRepository;
import com.padelconnect.repository.PartidoRepository;
import com.padelconnect.repository.UsuarioRepository;
import com.padelconnect.service.PartidoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A propósito NO usa @Transactional a nivel de clase: cada hilo necesita su propia
 * transacción/conexión real para que el lock pesimista (SELECT ... FOR UPDATE) sirva de algo.
 * Si todo el test corriera en una sola transacción (como el resto de los tests), los hilos
 * lanzados acá no verían los datos entre sí ni ejercitarían el lock real de la base.
 */
@SpringBootTest
class ConcurrenciaTest {

    @Autowired
    private PartidoService partidoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PartidoRepository partidoRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    private Long partidoId;

    @AfterEach
    void limpiar() {
        if (partidoId == null) return;
        partidoRepository.findById(partidoId).ifPresent(p -> {
            inscripcionRepository.deleteAll(inscripcionRepository.findByPartido(p));
            partidoRepository.delete(p);
        });
    }

    @Test
    void dosAceptacionesSimultaneasPorElUltimoCupoSoloUnaGana() throws Exception {
        Usuario org = usuarioRepository.save(new Usuario(null, "Org Concurrencia", "org-conc@test.com", "123456", "111", "5ta"));
        Usuario j1 = usuarioRepository.save(new Usuario(null, "J1 Concurrencia", "j1-conc@test.com", "123456", "222", "5ta"));
        Usuario j2 = usuarioRepository.save(new Usuario(null, "J2 Concurrencia", "j2-conc@test.com", "123456", "333", "5ta"));

        // cuposTotales = 2 (organizador + 1 lugar real disponible para pelear)
        PartidoRequestDTO req = PartidoRequestDTO.builder()
                .fecha(LocalDate.now().plusDays(10).toString()).hora("18:00")
                .cancha("Cancha Concurrencia").direccion("Calle 123").zona("CABA")
                .latitud(-34.6037).longitud(-58.3816).nivel("5ta")
                .jugadoresFaltantes(1).precioPersona("$4.000").tipoCancha("Sintetico")
                .build();
        PartidoResponseDTO partido = partidoService.crearPartido(req, org);
        partidoId = partido.getId();

        // j1 y j2 piden unirse: ambos quedan PENDIENTE, todavía no consumen el cupo real.
        partidoService.unirseAPartido(partidoId, j1, null);
        partidoService.unirseAPartido(partidoId, j2, null);

        List<Long> inscripcionesPendientes = inscripcionRepository.findByPartido(partidoRepository.findById(partidoId).orElseThrow())
                .stream()
                .filter(i -> i.getEstado() == EstadoInscripcion.PENDIENTE)
                .map(i -> i.getId())
                .toList();
        assertEquals(2, inscripcionesPendientes.size());

        // El organizador intenta aceptar las dos solicitudes AL MISMO TIEMPO, pero solo queda 1 cupo real.
        CountDownLatch listos = new CountDownLatch(2);
        CountDownLatch arranquen = new CountDownLatch(1);
        AtomicInteger exitos = new AtomicInteger(0);
        AtomicInteger sinCupo = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            List<Callable<Void>> tareas = inscripcionesPendientes.stream().<Callable<Void>>map(inscripcionId -> () -> {
                listos.countDown();
                arranquen.await();
                try {
                    partidoService.aceptarInscripcion(partidoId, inscripcionId, org);
                    exitos.incrementAndGet();
                } catch (CupoLlenoException e) {
                    sinCupo.incrementAndGet();
                }
                return null;
            }).toList();

            List<Future<Void>> futuros = tareas.stream().map(executor::submit).toList();
            listos.await();
            arranquen.countDown();
            for (Future<Void> futuro : futuros) {
                futuro.get(10, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdown();
        }

        assertEquals(1, exitos.get(), "de las dos aceptaciones simultáneas por el último cupo, solo una debería ganar");
        assertEquals(1, sinCupo.get(), "la otra debe fallar con CupoLlenoException, no quedar aceptada igual");

        long aceptadosFinal = inscripcionRepository.countByPartidoAndEstado(
                partidoRepository.findById(partidoId).orElseThrow(), EstadoInscripcion.ACEPTADO);
        assertEquals(2, aceptadosFinal, "debe haber exactamente 2 ACEPTADO: el organizador + el único que ganó la carrera");
    }
}
