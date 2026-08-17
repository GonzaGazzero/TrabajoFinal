package com.padelconnect;

import com.padelconnect.dto.PartidoRequestDTO;
import com.padelconnect.dto.PartidoResponseDTO;
import com.padelconnect.entity.EstadoInscripcion;
import com.padelconnect.entity.Usuario;
import com.padelconnect.exception.BadRequestException;
import com.padelconnect.exception.CupoLlenoException;
import com.padelconnect.repository.InscripcionRepository;
import com.padelconnect.repository.PartidoRepository;
import com.padelconnect.repository.UsuarioRepository;
import com.padelconnect.service.PartidoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PartidoServiceTest {

    @Autowired
    private PartidoService partidoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PartidoRepository partidoRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    private Usuario org;
    private Usuario j1;
    private Usuario j2;

    @BeforeEach
    void setUp() {
        org = usuarioRepository.save(new Usuario(null, "Organizador", "org@test.com", "123456", "111111", "5ta"));
        j1 = usuarioRepository.save(new Usuario(null, "Jugador 1", "j1@test.com", "123456", "222222", "5ta"));
        j2 = usuarioRepository.save(new Usuario(null, "Jugador 2", "j2@test.com", "123456", "333333", "5ta"));
    }

    @Test
    void testCrearPartidoConJugadoresFaltantes() {
        // Pedimos 2 jugadores faltantes. Cupos totales deben ser 3 (1 organizador + 2 faltantes).
        PartidoRequestDTO req = new PartidoRequestDTO("2026-08-20", "18:00", "Club Padel", "Calle 123", "CABA",
                -34.6037, -58.3816, "5ta", null, 2, "$4.000", "Sintetico");

        PartidoResponseDTO partido = partidoService.crearPartido(req, org);

        assertEquals(3, partido.getCuposTotales());
        assertEquals(2, partido.getJugadoresFaltantes());
        assertEquals(2, partido.getCuposDisponibles());
        assertEquals("ORGANIZADOR", partido.getEstadoInscripcionUsuario());
        assertEquals(1, partido.getJugadores().size());
    }

    @Test
    void testUnirsePartidoQuedaPendiente() {
        PartidoRequestDTO req = new PartidoRequestDTO("2026-08-20", "18:00", "Club Padel", "Calle 123", "CABA",
                -34.6037, -58.3816, "5ta", null, 2, "$4.000", "Sintetico");
        PartidoResponseDTO partido = partidoService.crearPartido(req, org);

        // j1 se une -> estado PENDIENTE
        PartidoResponseDTO resultJ1 = partidoService.unirseAPartido(partido.getId(), j1, null);

        assertEquals("PENDIENTE", resultJ1.getEstadoInscripcionUsuario());
        assertEquals(2, resultJ1.getJugadoresFaltantes()); // Sigue en 2 porque j1 aun no fue ACEPTADO

        // Organizador ve la solicitud pendiente
        PartidoResponseDTO respOrg = partidoService.obtenerPartidoPorId(partido.getId(), null, null, org);
        assertEquals(1, respOrg.getSolicitudesPendientes().size());
        assertEquals(j1.getId(), respOrg.getSolicitudesPendientes().get(0).getJugador().getId());
    }

    @Test
    void testAceptarInscripcion() {
        PartidoRequestDTO req = new PartidoRequestDTO("2026-08-20", "18:00", "Club Padel", "Calle 123", "CABA",
                -34.6037, -58.3816, "5ta", null, 1, "$4.000", "Sintetico");
        PartidoResponseDTO partido = partidoService.crearPartido(req, org);

        partidoService.unirseAPartido(partido.getId(), j1, null);
        PartidoResponseDTO respOrg = partidoService.obtenerPartidoPorId(partido.getId(), null, null, org);
        Long inscripcionId = respOrg.getSolicitudesPendientes().get(0).getId();

        // Organizador acepta a j1
        PartidoResponseDTO postAceptar = partidoService.aceptarInscripcion(partido.getId(), inscripcionId, org);

        assertEquals(0, postAceptar.getJugadoresFaltantes()); // Ya se completo
        assertEquals(0, postAceptar.getSolicitudesPendientes().size());
        assertEquals(2, postAceptar.getJugadores().size());
    }

    @Test
    void testNoOrganizadorNoPuedeAceptar() {
        PartidoRequestDTO req = new PartidoRequestDTO("2026-08-20", "18:00", "Club Padel", "Calle 123", "CABA",
                -34.6037, -58.3816, "5ta", null, 1, "$4.000", "Sintetico");
        PartidoResponseDTO partido = partidoService.crearPartido(req, org);

        partidoService.unirseAPartido(partido.getId(), j1, null);
        PartidoResponseDTO respOrg = partidoService.obtenerPartidoPorId(partido.getId(), null, null, org);
        Long inscripcionId = respOrg.getSolicitudesPendientes().get(0).getId();

        // j2 intenta aceptar -> debe fallar
        assertThrows(BadRequestException.class, () -> partidoService.aceptarInscripcion(partido.getId(), inscripcionId, j2));
    }

    @Test
    void testUnirseConMensajeQuedaGuardado() {
        PartidoRequestDTO req = new PartidoRequestDTO("2026-08-20", "18:00", "Club Padel", "Calle 123", "CABA",
                -34.6037, -58.3816, "5ta", null, 1, "$4.000", "Sintetico");
        PartidoResponseDTO partido = partidoService.crearPartido(req, org);

        partidoService.unirseAPartido(partido.getId(), j1, "Somos 2, jugamos hace 3 años");

        PartidoResponseDTO respOrg = partidoService.obtenerPartidoPorId(partido.getId(), null, null, org);
        assertEquals("Somos 2, jugamos hace 3 años", respOrg.getSolicitudesPendientes().get(0).getMensaje());
    }

    @Test
    void testEliminarPartidoPorOrganizador() {
        PartidoRequestDTO req = new PartidoRequestDTO("2026-08-20", "18:00", "Club Padel", "Calle 123", "CABA",
                -34.6037, -58.3816, "5ta", null, 1, "$4.000", "Sintetico");
        PartidoResponseDTO partido = partidoService.crearPartido(req, org);
        partidoService.unirseAPartido(partido.getId(), j1, null);

        partidoService.eliminarPartido(partido.getId(), org);

        assertTrue(partidoRepository.findById(partido.getId()).isEmpty());
        assertTrue(inscripcionRepository.findByPartido(
                partidoRepository.findById(partido.getId()).orElse(null)
        ).isEmpty());
    }

    @Test
    void testNoOrganizadorNoPuedeEliminar() {
        PartidoRequestDTO req = new PartidoRequestDTO("2026-08-20", "18:00", "Club Padel", "Calle 123", "CABA",
                -34.6037, -58.3816, "5ta", null, 1, "$4.000", "Sintetico");
        PartidoResponseDTO partido = partidoService.crearPartido(req, org);

        assertThrows(BadRequestException.class, () -> partidoService.eliminarPartido(partido.getId(), j1));
        assertTrue(partidoRepository.findById(partido.getId()).isPresent());
    }

    @Test
    void testNoSePuedeEliminarPartidoAMenosDeUnaHora() {
        String fechaHoy = LocalDate.now().toString();
        String horaEnMediaHora = LocalTime.now().plusMinutes(30).truncatedTo(ChronoUnit.MINUTES).toString();

        PartidoRequestDTO req = new PartidoRequestDTO(fechaHoy, horaEnMediaHora, "Club Padel", "Calle 123", "CABA",
                -34.6037, -58.3816, "5ta", null, 1, "$4.000", "Sintetico");
        PartidoResponseDTO partido = partidoService.crearPartido(req, org);

        assertThrows(BadRequestException.class, () -> partidoService.eliminarPartido(partido.getId(), org));
        assertTrue(partidoRepository.findById(partido.getId()).isPresent());
    }

    @Test
    void testListarPartidosExcluyeLlenos() {
        // jugadoresFaltantes = 1 -> cuposTotales = 2 (organizador + 1). Al aceptar a j1 queda lleno.
        PartidoRequestDTO req = new PartidoRequestDTO("2026-08-20", "18:00", "Club Padel Lleno", "Calle 123", "CABA",
                -34.6037, -58.3816, "5ta", null, 1, "$4.000", "Sintetico");
        PartidoResponseDTO partidoLleno = partidoService.crearPartido(req, org);

        partidoService.unirseAPartido(partidoLleno.getId(), j1, null);
        PartidoResponseDTO respOrg = partidoService.obtenerPartidoPorId(partidoLleno.getId(), null, null, org);
        Long inscripcionId = respOrg.getSolicitudesPendientes().get(0).getId();
        PartidoResponseDTO lleno = partidoService.aceptarInscripcion(partidoLleno.getId(), inscripcionId, org);
        assertEquals(0, lleno.getCuposDisponibles());

        List<PartidoResponseDTO> listado = partidoService.listarPartidos(null, null, null, null, null, null, null, org);

        assertTrue(listado.stream().noneMatch(p -> p.getId().equals(partidoLleno.getId())));
    }
}
