package com.padelconnect.controller;

import com.padelconnect.dto.AuthResponse;
import com.padelconnect.dto.LoginRequest;
import com.padelconnect.dto.RegisterRequest;
import com.padelconnect.dto.UserDto;
import com.padelconnect.entity.Usuario;
import com.padelconnect.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal Usuario currentUser) {
        UserDto userDto = authService.getPerfil(currentUser);
        return ResponseEntity.ok(userDto);
    }
}
