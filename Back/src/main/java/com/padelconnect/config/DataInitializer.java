package com.padelconnect.config;

import com.padelconnect.entity.EstadoInscripcion;
import com.padelconnect.entity.Inscripcion;
import com.padelconnect.entity.Partido;
import com.padelconnect.entity.Usuario;
import com.padelconnect.repository.InscripcionRepository;
import com.padelconnect.repository.PartidoRepository;
import com.padelconnect.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PartidoRepository partidoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           PartidoRepository partidoRepository,
                           InscripcionRepository inscripcionRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.partidoRepository = partidoRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Si ya hay usuarios cargados (ej: perfil con base persistente que ya arrancó antes),
        // no se vuelve a cargar nada para no duplicar ni romper el email único.
        if (usuarioRepository.count() > 0) {
            return;
        }

        String claveDePrueba = passwordEncoder.encode("Padel123");

        Usuario gonzalo = usuarioRepository.save(new Usuario(null, "Gonzalo Gazzero", "gonzalo@padeltest.com",
                claveDePrueba, "3467435723", "5ta Categoría"));
        Usuario juanito = usuarioRepository.save(new Usuario(null, "Juanito Lopez", "juanito@padeltest.com",
                claveDePrueba, "3467111111", "6ta Categoría"));
        Usuario marcos = usuarioRepository.save(new Usuario(null, "Marcos Paredes", "marcos@padeltest.com",
                claveDePrueba, "3467222222", "4ta Categoría"));
        Usuario andres = usuarioRepository.save(new Usuario(null, "Andres Gazzero", "andres@padeltest.com",
                claveDePrueba, "3467333333", "7ma Categoría"));

        LocalDate hoy = LocalDate.now();
        String ayer = hoy.minusDays(1).toString();
        String manana = hoy.plusDays(1).toString();
        String enTresDias = hoy.plusDays(3).toString();
        String enUnaSemana = hoy.plusDays(7).toString();
        String enDosSemanas = hoy.plusDays(14).toString();

        // Partido abierto, sin solicitudes todavía - Rosario
        Partido partido1 = partidoRepository.save(new Partido(null, enTresDias, "19:00", "Náutico Rosario",
                "Av. Costanera 500", "Rosario (Santa Fe)", -32.9442, -60.6505, "6ta Categoría", 4,
                "$6.000", "Cemento", "Mixto", juanito));
        inscripcionRepository.save(new Inscripcion(partido1, juanito, EstadoInscripcion.ACEPTADO));

        // Partido abierto, sin solicitudes todavía - Villa María
        Partido partido2 = partidoRepository.save(new Partido(null, enUnaSemana, "20:00", "Club Villa María Pádel",
                "Bv. Sarmiento 850", "Villa María (Córdoba)", -32.4135041930881, -63.248329032642, "7ma Categoría", 2,
                "$5.000", "Césped Sintético", "Masculino", andres));
        inscripcionRepository.save(new Inscripcion(partido2, andres, EstadoInscripcion.ACEPTADO));

        // Partido con una solicitud pendiente (con mensaje incluido) - Monte Buey
        Partido partido3 = partidoRepository.save(new Partido(null, manana, "18:30", "Complejo Monte Buey",
                "Ruta 6 Km 4", "Monte Buey (Córdoba)", -32.9156374383737, -62.454900955381, "5ta Categoría", 2,
                "$4.500", "Césped Sintético", "Mixto", gonzalo));
        inscripcionRepository.save(new Inscripcion(partido3, gonzalo, EstadoInscripcion.ACEPTADO));
        Inscripcion solicitud = new Inscripcion(partido3, juanito, EstadoInscripcion.PENDIENTE);
        solicitud.setMensaje("Hola! Somos 2, jugamos hace un año a nivel 5ta. ¿Se puede sumar mi pareja de juego también?");
        inscripcionRepository.save(solicitud);

        // Partido con un jugador ya aceptado y todavía con lugar - Córdoba Capital
        Partido partido4 = partidoRepository.save(new Partido(null, enDosSemanas, "21:00", "Pádel Nueva Córdoba",
                "Bv. Illia 234", "Córdoba Capital (Córdoba)", -31.4201, -64.1888, "4ta Categoría", 3,
                "$7.000", "Cemento", "Femenino", marcos));
        inscripcionRepository.save(new Inscripcion(partido4, marcos, EstadoInscripcion.ACEPTADO));
        inscripcionRepository.save(new Inscripcion(partido4, andres, EstadoInscripcion.ACEPTADO));

        // Partido lleno, para probar que no aparece en el listado - Monte Buey
        Partido partido5 = partidoRepository.save(new Partido(null, enTresDias, "10:00", "Complejo Monte Buey",
                "Ruta 6 Km 4", "Monte Buey (Córdoba)", -32.9156374383737, -62.454900955381, "6ta Categoría", 2,
                "$4.500", "Césped Sintético", "Mixto", andres));
        inscripcionRepository.save(new Inscripcion(partido5, andres, EstadoInscripcion.ACEPTADO));
        inscripcionRepository.save(new Inscripcion(partido5, gonzalo, EstadoInscripcion.ACEPTADO));

        // Partido de ayer, para probar que no aparece en el listado - Rosario
        Partido partido6 = partidoRepository.save(new Partido(null, ayer, "18:00", "Náutico Rosario",
                "Av. Costanera 500", "Rosario (Santa Fe)", -32.9442, -60.6505, "5ta Categoría", 4,
                "$6.000", "Cemento", "Mixto", marcos));
        inscripcionRepository.save(new Inscripcion(partido6, marcos, EstadoInscripcion.ACEPTADO));
    }
}
