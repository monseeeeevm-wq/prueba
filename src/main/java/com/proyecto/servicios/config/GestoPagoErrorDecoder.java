package com.proyecto.servicios.config;

import com.proyecto.servicios.exception.GestoPagoException;
import com.proyecto.servicios.exception.GestoPagoException.Tipo;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GestoPagoErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        log.error("Error de integracion GestoPago. metodo={}, status={}", methodKey, status);

        if (status == 401 || status == 403) {
            return new GestoPagoException(Tipo.AUTENTICACION,
                    "Error de autenticacion con GestoPago (token invalido o expirado)", status);
        }
        return new GestoPagoException(Tipo.RESPUESTA_NO_EXITOSA,
                "GestoPago respondio con estatus no exitoso", status);
    }
}