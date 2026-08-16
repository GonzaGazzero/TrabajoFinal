package com.padelconnect.controller;

import com.padelconnect.dto.MisPartidosDTO;
import com.padelconnect.dto.PartidoRequestDTO;
import com.padelconnect.dto.PartidoResponseDTO;
import com.padelconnect.entity.Usuario;
import com.padelconnect.service.PartidoService;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/partidos")
public class PartidoController {

    private final PartidoService partidoService;
    private final MessageSource messageSource;

    public PartidoController(PartidoService partidoService, MessageSource messageSource) {
        this.partidoService = partidoService;
        this.messageSource = messageSource;
    }

    @GetMapping
    public ResponseEntity<List<PartidoResponseDTO>> listarPartidos(
            @RequestParam(required = false) String zona,
            @RequestParam(required = false) String fecha,
            @RequestParam(required = false) String nivel,
            @RequestParam(required = false) String genero,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) String orden,
            @AuthenticationPrincipal Usuario currentUser) {
        List<PartidoResponseDTO> partidos = partidoService.listarPartidos(zona, fecha, nivel, genero, lat, lng, orden, currentUser);
        return ResponseEntity.ok(partidos);
    }

    @GetMapping("/mis-partidos")
    public ResponseEntity<MisPartidosDTO> misPartidos(
            @AuthenticationPrincipal Usuario currentUser,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng) {
        MisPartidosDTO misPartidos = partidoService.obtenerMisPartidos(currentUser, lat, lng);
        return ResponseEntity.ok(misPartidos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartidoResponseDTO> obtenerPartido(
            @PathVariable Long id,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @AuthenticationPrincipal Usuario currentUser) {
        PartidoResponseDTO partido = partidoService.obtenerPartidoPorId(id, lat, lng, currentUser);
        return ResponseEntity.ok(partido);
    }

    @PostMapping
    public ResponseEntity<PartidoResponseDTO> crearPartido(
            @Valid @RequestBody PartidoRequestDTO dto,
            @AuthenticationPrincipal Usuario currentUser) {
        PartidoResponseDTO creado = partidoService.crearPartido(dto, currentUser);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/unirse")
    public ResponseEntity<PartidoResponseDTO> unirseAPartido(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario currentUser) {
        PartidoResponseDTO partido = partidoService.unirseAPartido(id, currentUser);
        return ResponseEntity.ok(partido);
    }

    @PostMapping("/{id}/inscripciones/{inscripcionId}/aceptar")
    public ResponseEntity<PartidoResponseDTO> aceptarInscripcion(
            @PathVariable Long id,
            @PathVariable Long inscripcionId,
            @AuthenticationPrincipal Usuario currentUser) {
        PartidoResponseDTO partido = partidoService.aceptarInscripcion(id, inscripcionId, currentUser);
        return ResponseEntity.ok(partido);
    }

    @PostMapping("/{id}/inscripciones/{inscripcionId}/rechazar")
    public ResponseEntity<PartidoResponseDTO> rechazarInscripcion(
            @PathVariable Long id,
            @PathVariable Long inscripcionId,
            @AuthenticationPrincipal Usuario currentUser) {
        PartidoResponseDTO partido = partidoService.rechazarInscripcion(id, inscripcionId, currentUser);
        return ResponseEntity.ok(partido);
    }

    @PostMapping("/{id}/salir")
    public ResponseEntity<Map<String, String>> salirDePartido(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario currentUser) {
        partidoService.salirDePartido(id, currentUser);
        String msg = messageSource.getMessage("message.match.left", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(Map.of("message", msg));
    }
}
