package com.proyecto.servicios.config;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/**
 * Sin @Configuration a proposito: solo aplica al cliente que la referencia,
 * no a los demas Feign clients (por ejemplo, el de autenticacion).
 */
public class GestoPagoProductClientConfig {

    private static final String BEARER_PREFIX = "Bearer ";

    @Bean
    public RequestInterceptor gestoPagoBearerInterceptor(@Value("${gestopago.api.token}") String token) {
        return template -> template.header("Authorization", BEARER_PREFIX + token);
    }

    @Bean
    public ErrorDecoder gestoPagoErrorDecoder() {
        return new GestoPagoErrorDecoder();
    }
}