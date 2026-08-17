package com.padelconnect.service;

import com.padelconnect.dto.*;
import com.padelconnect.entity.EstadoInscripcion;
import com.padelconnect.entity.Inscripcion;
import com.padelconnect.entity.Partido;
import com.padelconnect.entity.Usuario;
import com.padelconnect.exception.AlreadyJoinedException;
import com.padelconnect.exception.BadRequestException;
import com.padelconnect.exception.CupoLlenoException;
import com.padelconnect.exception.ResourceNotFoundException;
import com.padelconnect.repository.InscripcionRepository;
import com.padelconnect.repository.PartidoRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PartidoService {

    private final PartidoRepository partidoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final HaversineService haversineService;
    private final GeocodingService geocodingService;
    private final MessageSource messageSource;

    public PartidoService(PartidoRepository partidoRepository,
                          InscripcionRepository inscripcionRepository,
                          HaversineService haversineService,
                          GeocodingService geocodingService,
                          MessageSource messageSource) {
        this.partidoRepository = partidoRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.haversineService = haversineService;
        this.geocodingService = geocodingService;
        this.messageSource = messageSource;
    }

    @Transactional(readOnly = true)
    public List<PartidoResponseDTO> listarPartidos(String zona, String fecha, String nivel, String genero,
                                                     Double userLat, Double userLng, String orden, Usuario currentUser) {
        List<Partido> partidos;

        boolean tieneZona = zona != null && !zona.isBlank() && !"Todas las zonas".equalsIgnoreCase(zona);
        boolean tieneFecha = fecha != null && !fecha.isBlank();
        boolean tieneNivel = nivel != null && !nivel.isBlank() && !"Todos los niveles".equalsIgnoreCase(nivel);
        boolean tieneGenero = genero != null && !genero.isBlank() && !"Todos".equalsIgnoreCase(genero);

        if (tieneZona && tieneFecha) {
            partidos = partidoRepository.findByZonaAndFecha(zona, fecha);
        } else if (tieneZona) {
            partidos = partidoRepository.findByZona(zona);
        } else if (tieneFecha) {
            partidos = partidoRepository.findByFecha(fecha);
        } else {
            partidos = partidoRepository.findAll();
        }

        if (tieneNivel) {
            partidos = partidos.stream()
                    .filter(p -> nivel.equalsIgnoreCase(p.getNivel()))
                    .collect(Collectors.toList());
        }

        if (tieneGenero) {
            partidos = partidos.stream()
                    .filter(p -> genero.equalsIgnoreCase(p.getGenero()))
                    .collect(Collectors.toList());
        }

        List<PartidoResponseDTO> responseDTOs = partidos.stream()
                .map(partido -> buildPartidoResponseDTO(partido, userLat, userLng, currentUser))
                .filter(dto -> dto.getCuposDisponibles() == null || dto.getCuposDisponibles() > 0)
                .collect(Collectors.toList());

        if ("cercania".equalsIgnoreCase(orden) || userLat != null) {
            responseDTOs.sort(Comparator.comparing(PartidoResponseDTO::getDistanciaKm, Comparator.nullsLast(Double::compareTo)));
        }

        return responseDTOs;
    }

    @Transactional(readOnly = true)
    public PartidoResponseDTO obtenerPartidoPorId(Long id, Double userLat, Double userLng, Usuario currentUser) {
        String msg = messageSource.getMessage("error.match.notfound", null, LocaleContextHolder.getLocale());
        Partido partido = partidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(msg));
        return buildPartidoResponseDTO(partido, userLat, userLng, currentUser);
    }

    @Transactional
    public PartidoResponseDTO crearPartido(PartidoRequestDTO dto, Usuario organizador) {
        if (organizador == null) {
            String msg = messageSource.getMessage("error.user.unauthorized", null, LocaleContextHolder.getLocale());
            throw new BadRequestException(msg);
        }

        Double lat = dto.getLatitud();
        Double lng = dto.getLongitud();

        if (lat == null || lng == null || (Math.abs(lat - (-34.6037)) < 0.0001 && Math.abs(lng - (-58.3816)) < 0.0001 && dto.getZona() != null && !dto.getZona().contains("CABA"))) {
            double[] coords = geocodingService.obtenerCoordenadas(dto.getZona());
            lat = coords[0];
            lng = coords[1];
        }

        int cuposTotales;
        if (dto.getJugadoresFaltantes() != null && dto.getJugadoresFaltantes() > 0) {
            cuposTotales = dto.getJugadoresFaltantes() + 1;
        } else if (dto.getCuposTotales() != null && dto.getCuposTotales() > 0) {
            cuposTotales = dto.getCuposTotales();
        } else {
            cuposTotales = 4;
        }

        Partido partido = new Partido(
                null,
                dto.getFecha(),
                dto.getHora(),
                dto.getCancha(),
                dto.getDireccion() != null ? dto.getDireccion() : "Dirección a confirmar",
                dto.getZona(),
                lat,
                lng,
                dto.getNivel(),
                cuposTotales,
                dto.getPrecioPersona() != null ? dto.getPrecioPersona() : "$4.000",
                dto.getTipoCancha() != null ? dto.getTipoCancha() : "Césped Sintético",
                dto.getGenero() != null && !dto.getGenero().isBlank() ? dto.getGenero() : "Mixto",
                organizador
        );

        Partido partidoGuardado = partidoRepository.save(partido);

        Inscripcion inscripcionOrganizador = new Inscripcion(partidoGuardado, organizador, EstadoInscripcion.ACEPTADO);
        inscripcionRepository.save(inscripcionOrganizador);

        return buildPartidoResponseDTO(partidoGuardado, dto.getLatitud(), dto.getLongitud(), organizador);
    }

    @Transactional
    public PartidoResponseDTO unirseAPartido(Long partidoId, Usuario usuario, String mensaje) {
        if (usuario == null) {
            String msg = messageSource.getMessage("error.user.unauthorized", null, LocaleContextHolder.getLocale());
            throw new BadRequestException(msg);
        }

        String notFoundMsg = messageSource.getMessage("error.match.notfound", null, LocaleContextHolder.getLocale());
        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new ResourceNotFoundException(notFoundMsg));

        long inscriptosAceptados = inscripcionRepository.countByPartidoAndEstado(partido, EstadoInscripcion.ACEPTADO);
        int cuposDisponibles = partido.getCuposTotales() - (int) inscriptosAceptados;

        if (cuposDisponibles <= 0) {
            String fullMsg = messageSource.getMessage("error.match.full", null, LocaleContextHolder.getLocale());
            throw new CupoLlenoException(fullMsg);
        }

        Optional<Inscripcion> inscripcionExistente = inscripcionRepository.findByPartidoAndJugador(partido, usuario);
        if (inscripcionExistente.isPresent()) {
            Inscripcion inc = inscripcionExistente.get();
            if (inc.getEstado() == EstadoInscripcion.PENDIENTE || inc.getEstado() == EstadoInscripcion.ACEPTADO) {
                String alreadyJoinedMsg = messageSource.getMessage("error.match.already_joined", null, LocaleContextHolder.getLocale());
                throw new AlreadyJoinedException(alreadyJoinedMsg);
            } else {
                inc.setEstado(EstadoInscripcion.PENDIENTE);
                inc.setMensaje(mensaje);
                inscripcionRepository.save(inc);
                return buildPartidoResponseDTO(partido, null, null, usuario);
            }
        }

        Inscripcion nuevaInscripcion = new Inscripcion(partido, usuario, EstadoInscripcion.PENDIENTE);
        nuevaInscripcion.setMensaje(mensaje);
        inscripcionRepository.save(nuevaInscripcion);

        return buildPartidoResponseDTO(partido, null, null, usuario);
    }

    @Transactional
    public PartidoResponseDTO aceptarInscripcion(Long partidoId, Long inscripcionId, Usuario currentUser) {
        if (currentUser == null) {
            String msg = messageSource.getMessage("error.user.unauthorized", null, LocaleContextHolder.getLocale());
            throw new BadRequestException(msg);
        }

        String notFoundMsg = messageSource.getMessage("error.match.notfound", null, LocaleContextHolder.getLocale());
        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new ResourceNotFoundException(notFoundMsg));

        if (!partido.getOrganizador().getId().equals(currentUser.getId())) {
            String notOrgMsg = messageSource.getMessage("error.match.not_organizer", null, LocaleContextHolder.getLocale());
            throw new BadRequestException(notOrgMsg);
        }

        Inscripcion inscripcion = inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("error.inscription.notfound", null, LocaleContextHolder.getLocale())));

        if (!inscripcion.getPartido().getId().equals(partidoId)) {
            throw new ResourceNotFoundException(messageSource.getMessage("error.inscription.notfound", null, LocaleContextHolder.getLocale()));
        }

        long aceptados = inscripcionRepository.countByPartidoAndEstado(partido, EstadoInscripcion.ACEPTADO);
        if (aceptados >= partido.getCuposTotales()) {
            String fullMsg = messageSource.getMessage("error.match.full", null, LocaleContextHolder.getLocale());
            throw new CupoLlenoException(fullMsg);
        }

        inscripcion.setEstado(EstadoInscripcion.ACEPTADO);
        inscripcionRepository.save(inscripcion);

        return buildPartidoResponseDTO(partido, null, null, currentUser);
    }

    @Transactional
    public PartidoResponseDTO rechazarInscripcion(Long partidoId, Long inscripcionId, Usuario currentUser) {
        if (currentUser == null) {
            String msg = messageSource.getMessage("error.user.unauthorized", null, LocaleContextHolder.getLocale());
            throw new BadRequestException(msg);
        }

        String notFoundMsg = messageSource.getMessage("error.match.notfound", null, LocaleContextHolder.getLocale());
        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new ResourceNotFoundException(notFoundMsg));

        if (!partido.getOrganizador().getId().equals(currentUser.getId())) {
            String notOrgMsg = messageSource.getMessage("error.match.not_organizer", null, LocaleContextHolder.getLocale());
            throw new BadRequestException(notOrgMsg);
        }

        Inscripcion inscripcion = inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("error.inscription.notfound", null, LocaleContextHolder.getLocale())));

        if (!inscripcion.getPartido().getId().equals(partidoId)) {
            throw new ResourceNotFoundException(messageSource.getMessage("error.inscription.notfound", null, LocaleContextHolder.getLocale()));
        }

        inscripcion.setEstado(EstadoInscripcion.RECHAZADO);
        inscripcionRepository.save(inscripcion);

        return buildPartidoResponseDTO(partido, null, null, currentUser);
    }

    @Transactional
    public void salirDePartido(Long partidoId, Usuario usuario) {
        if (usuario == null) {
            String msg = messageSource.getMessage("error.user.unauthorized", null, LocaleContextHolder.getLocale());
            throw new BadRequestException(msg);
        }

        String notFoundMsg = messageSource.getMessage("error.match.notfound", null, LocaleContextHolder.getLocale());
        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new ResourceNotFoundException(notFoundMsg));

        String notJoinedMsg = messageSource.getMessage("error.match.not_joined", null, LocaleContextHolder.getLocale());
        Inscripcion inscripcion = inscripcionRepository.findByPartidoAndJugador(partido, usuario)
                .orElseThrow(() -> new BadRequestException(notJoinedMsg));

        inscripcionRepository.delete(inscripcion);
    }

    @Transactional
    public void eliminarPartido(Long partidoId, Usuario currentUser) {
        if (currentUser == null) {
            String msg = messageSource.getMessage("error.user.unauthorized", null, LocaleContextHolder.getLocale());
            throw new BadRequestException(msg);
        }

        String notFoundMsg = messageSource.getMessage("error.match.notfound", null, LocaleContextHolder.getLocale());
        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new ResourceNotFoundException(notFoundMsg));

        if (!partido.getOrganizador().getId().equals(currentUser.getId())) {
            String notOrgMsg = messageSource.getMessage("error.match.not_organizer", null, LocaleContextHolder.getLocale());
            throw new BadRequestException(notOrgMsg);
        }

        LocalDateTime inicio = LocalDateTime.parse(partido.getFecha() + "T" + partido.getHora());
        if (Duration.between(LocalDateTime.now(), inicio).toMinutes() < 60) {
            String tooLateMsg = messageSource.getMessage("error.match.delete_too_late", null, LocaleContextHolder.getLocale());
            throw new BadRequestException(tooLateMsg);
        }

        inscripcionRepository.deleteAll(inscripcionRepository.findByPartido(partido));
        partidoRepository.delete(partido);
    }

    @Transactional(readOnly = true)
    public MisPartidosDTO obtenerMisPartidos(Usuario usuario, Double userLat, Double userLng) {
        if (usuario == null) {
            String msg = messageSource.getMessage("error.user.unauthorized", null, LocaleContextHolder.getLocale());
            throw new BadRequestException(msg);
        }

        List<Partido> partidosCreados = partidoRepository.findByOrganizador(usuario);
        List<PartidoResponseDTO> creadosDTO = partidosCreados.stream()
                .map(p -> buildPartidoResponseDTO(p, userLat, userLng, usuario))
                .collect(Collectors.toList());

        List<Inscripcion> inscripciones = inscripcionRepository.findByJugador(usuario);
        List<PartidoResponseDTO> unidosDTO = inscripciones.stream()
                .map(Inscripcion::getPartido)
                .filter(p -> !p.getOrganizador().getId().equals(usuario.getId()))
                .distinct()
                .map(p -> buildPartidoResponseDTO(p, userLat, userLng, usuario))
                .collect(Collectors.toList());

        return new MisPartidosDTO(creadosDTO, unidosDTO);
    }

    private PartidoResponseDTO buildPartidoResponseDTO(Partido partido, Double userLat, Double userLng, Usuario currentUser) {
        List<Inscripcion> todasInscripciones = inscripcionRepository.findByPartido(partido);

        List<JugadorDTO> jugadoresAceptados = new ArrayList<>();
        List<InscripcionDTO> solicitudesPendientes = new ArrayList<>();

        String estadoUsuario = "NINGUNA";

        for (Inscripcion inc : todasInscripciones) {
            Usuario j = inc.getJugador();
            boolean esOrganizador = j.getId().equals(partido.getOrganizador().getId());

            if (inc.getEstado() == EstadoInscripcion.ACEPTADO) {
                String rol = esOrganizador ? "Organizador" : "Jugador";
                jugadoresAceptados.add(JugadorDTO.fromEntity(j, rol));
            } else if (inc.getEstado() == EstadoInscripcion.PENDIENTE) {
                solicitudesPendientes.add(InscripcionDTO.fromEntity(inc));
            }

            if (currentUser != null && j.getId().equals(currentUser.getId())) {
                if (esOrganizador) {
                    estadoUsuario = "ORGANIZADOR";
                } else {
                    estadoUsuario = inc.getEstado().name();
                }
            }
        }

        if (currentUser != null && partido.getOrganizador().getId().equals(currentUser.getId())) {
            estadoUsuario = "ORGANIZADOR";
        }

        int cuposDisponibles = partido.getCuposTotales() - jugadoresAceptados.size();
        if (cuposDisponibles < 0) cuposDisponibles = 0;
        int jugadoresFaltantes = cuposDisponibles;

        Double matchLat = partido.getLatitud();
        Double matchLng = partido.getLongitud();

        if (matchLat == null || matchLng == null || (Math.abs(matchLat - (-34.6037)) < 0.0001 && Math.abs(matchLng - (-58.3816)) < 0.0001 && partido.getZona() != null && !partido.getZona().contains("CABA"))) {
            double[] coords = geocodingService.obtenerCoordenadas(partido.getZona());
            matchLat = coords[0];
            matchLng = coords[1];
        }

        Double distanciaKm = haversineService.calcularDistanciaKm(
                userLat, userLng,
                matchLat, matchLng
        );

        return new PartidoResponseDTO(
                partido.getId(),
                partido.getFecha(),
                partido.getHora(),
                partido.getCancha(),
                partido.getDireccion(),
                partido.getZona(),
                matchLat,
                matchLng,
                partido.getNivel(),
                partido.getCuposTotales(),
                cuposDisponibles,
                jugadoresFaltantes,
                distanciaKm,
                partido.getPrecioPersona(),
                partido.getTipoCancha(),
                partido.getGenero(),
                UserDto.fromEntity(partido.getOrganizador()),
                jugadoresAceptados,
                solicitudesPendientes,
                estadoUsuario
        );
    }
}
