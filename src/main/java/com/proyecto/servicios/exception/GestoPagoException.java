package com.proyecto.servicios.exception;

import lombok.Getter;

@Getter
public class GestoPagoException extends RuntimeException {

    public enum Tipo {
        AUTENTICACION,
        COMUNICACION,
        TIMEOUT,
        RESPUESTA_NO_EXITOSA
    }

    private final Tipo tipo;
    private final int status;

    public GestoPagoException(Tipo tipo, String message, int status) {
        super(message);
        this.tipo = tipo;
        this.status = status;
    }

    public GestoPagoException(Tipo tipo, String message, Throwable cause) {
        super(message, cause);
        this.tipo = tipo;
        this.status = 0;
    }
}