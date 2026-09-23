package com.padelconnect.dto;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class ErrorResponseDTO {

    private final int status;
    private final String error;
    private final String message;
    private final LocalDateTime timestamp = LocalDateTime.now();

    public ErrorResponseDTO(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
    }
}
