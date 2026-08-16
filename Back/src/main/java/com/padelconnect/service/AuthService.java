package com.padelconnect.service;

import com.padelconnect.config.JwtTokenProvider;
import com.padelconnect.dto.AuthResponse;
import com.padelconnect.dto.LoginRequest;
import com.padelconnect.dto.RegisterRequest;
import com.padelconnect.dto.UserDto;
import com.padelconnect.entity.Usuario;
import com.padelconnect.exception.BadRequestException;
import com.padelconnect.repository.UsuarioRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final MessageSource messageSource;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       MessageSource messageSource) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.messageSource = messageSource;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.getConfirmPassword() == null || !request.getPassword().equals(request.getConfirmPassword())) {
            String msg = messageSource.getMessage("error.password.mismatch", null, LocaleContextHolder.getLocale());
            throw new BadRequestException(msg);
        }

        if (usuarioRepository.existsByEmail(request.getEmail())) {
            String msg = messageSource.getMessage("error.email.exists", null, LocaleContextHolder.getLocale());
            throw new BadRequestException(msg);
        }

        String passwordEncoded = passwordEncoder.encode(request.getPassword());
        String telefono = (request.getTelefono() != null && !request.getTelefono().isBlank()) 
                ? request.getTelefono() 
                : "5491100000000";
        String nivel = (request.getNivel() != null && !request.getNivel().isBlank()) 
                ? request.getNivel() 
                : "Principiante (6ta - 7ma)";

        Usuario nuevoUsuario = new Usuario(
                null,
                request.getNombre(),
                request.getEmail(),
                passwordEncoded,
                telefono,
                nivel
        );

        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);
        String token = tokenProvider.generateToken(usuarioGuardado.getEmail());

        return new AuthResponse(token, UserDto.fromEntity(usuarioGuardado));
    }

    public AuthResponse login(LoginRequest request) {
        String badCredsMsg = messageSource.getMessage("error.user.bad_credentials", null, LocaleContextHolder.getLocale());

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException(badCredsMsg));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new BadCredentialsException(badCredsMsg);
        }

        String token = tokenProvider.generateToken(usuario.getEmail());
        return new AuthResponse(token, UserDto.fromEntity(usuario));
    }

    public UserDto getPerfil(Usuario usuario) {
        if (usuario == null) {
            String msg = messageSource.getMessage("error.user.unauthorized", null, LocaleContextHolder.getLocale());
            throw new BadRequestException(msg);
        }
        return UserDto.fromEntity(usuario);
    }
}
