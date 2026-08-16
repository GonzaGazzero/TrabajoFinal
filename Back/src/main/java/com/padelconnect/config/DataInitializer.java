package com.padelconnect.config;

import com.padelconnect.entity.Inscripcion;
import com.padelconnect.entity.Partido;
import com.padelconnect.entity.Usuario;
import com.padelconnect.repository.InscripcionRepository;
import com.padelconnect.repository.PartidoRepository;
import com.padelconnect.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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
    }
}
