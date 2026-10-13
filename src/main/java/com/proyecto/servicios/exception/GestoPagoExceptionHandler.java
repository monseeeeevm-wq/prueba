package com.proyecto.servicios.exception;

import com.proyecto.servicios.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GestoPagoExceptionHandler {

    @ExceptionHandler(GestoPagoException.class)
    public ResponseEntity<ErrorResponse> handle(GestoPagoException e) {
        HttpStatus status = switch (e.getTipo()) {
            case TIMEOUT -> HttpStatus.GATEWAY_TIMEOUT;
            case COMUNICACION -> HttpStatus.SERVICE_UNAVAILABLE;
            case AUTENTICACION, RESPUESTA_NO_EXITOSA -> HttpStatus.BAD_GATEWAY;
        };
        return ResponseEntity.status(status)
                .body(new ErrorResponse(e.getTipo().name(), e.getMessage()));
    }
}
