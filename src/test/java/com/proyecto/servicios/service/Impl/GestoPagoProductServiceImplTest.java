package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.client.GestoPagoProductClient;
import com.proyecto.servicios.exception.GestoPagoException;
import com.proyecto.servicios.exception.GestoPagoException.Tipo;
import com.proyecto.servicios.model.gestopago.MensajeDto;
import com.proyecto.servicios.model.gestopago.ProductListResponse;
import com.proyecto.servicios.model.gestopago.ProductoDto;
import feign.FeignException;
import feign.Request;
import feign.RetryableException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestoPagoProductServiceImplTest {

    @Mock
    private GestoPagoProductClient productClient;

    @InjectMocks
    private GestoPagoProductServiceImpl service;

    @Test
    @DisplayName("Respuesta exitosa: devuelve la lista de productos")
    void obtenerListaProductos_exito() {
        ProductListResponse esperada = respuesta("01", List.of(producto(56, 185), producto(71, 200)));
        when(productClient.getProductList()).thenReturn(esperada);

        ProductListResponse resultado = service.obtenerListaProductos();

        assertSame(esperada, resultado);
        assertEquals(2, resultado.getProductos().size());
        verify(productClient, times(1)).getProductList();
    }

    @Test
    @DisplayName("Codigo distinto de 01: lanza RESPUESTA_NO_EXITOSA")
    void obtenerListaProductos_codigoNoExitoso() {
        when(productClient.getProductList()).thenReturn(respuesta("99", List.of()));

        GestoPagoException ex = assertThrows(GestoPagoException.class,
                () -> service.obtenerListaProductos());

        assertEquals(Tipo.RESPUESTA_NO_EXITOSA, ex.getTipo());
    }

    @Test
    @DisplayName("Respuesta nula: lanza RESPUESTA_NO_EXITOSA")
    void obtenerListaProductos_respuestaNula() {
        when(productClient.getProductList()).thenReturn(null);

        GestoPagoException ex = assertThrows(GestoPagoException.class,
                () -> service.obtenerListaProductos());

        assertEquals(Tipo.RESPUESTA_NO_EXITOSA, ex.getTipo());
    }

    @Test
    @DisplayName("Respuesta sin MENSAJE: lanza RESPUESTA_NO_EXITOSA")
    void obtenerListaProductos_sinMensaje() {
        when(productClient.getProductList()).thenReturn(new ProductListResponse());

        GestoPagoException ex = assertThrows(GestoPagoException.class,
                () -> service.obtenerListaProductos());

        assertEquals(Tipo.RESPUESTA_NO_EXITOSA, ex.getTipo());
    }

    @Test
    @DisplayName("Timeout: lanza TIMEOUT")
    void obtenerListaProductos_timeout() {
        when(productClient.getProductList())
                .thenThrow(retryable(new SocketTimeoutException("Read timed out")));

        GestoPagoException ex = assertThrows(GestoPagoException.class,
                () -> service.obtenerListaProductos());

        assertEquals(Tipo.TIMEOUT, ex.getTipo());
    }

    @Test
    @DisplayName("Error de red: lanza COMUNICACION")
    void obtenerListaProductos_errorDeComunicacion() {
        when(productClient.getProductList())
                .thenThrow(retryable(new ConnectException("Connection refused")));

        GestoPagoException ex = assertThrows(GestoPagoException.class,
                () -> service.obtenerListaProductos());

        assertEquals(Tipo.COMUNICACION, ex.getTipo());
    }

    @Test
    @DisplayName("Token invalido o expirado: se propaga AUTENTICACION")
    void obtenerListaProductos_errorDeAutenticacion() {
        when(productClient.getProductList()).thenThrow(
                new GestoPagoException(Tipo.AUTENTICACION, "Token expirado", 403));

        GestoPagoException ex = assertThrows(GestoPagoException.class,
                () -> service.obtenerListaProductos());

        assertEquals(Tipo.AUTENTICACION, ex.getTipo());
        assertEquals(403, ex.getStatus());
    }

    @Test
    @DisplayName("Error HTTP generico de Feign: lanza RESPUESTA_NO_EXITOSA")
    void obtenerListaProductos_errorFeignGenerico() {
        FeignException feignError = mock(FeignException.class);
        when(feignError.status()).thenReturn(500);
        when(productClient.getProductList()).thenThrow(feignError);

        GestoPagoException ex = assertThrows(GestoPagoException.class,
                () -> service.obtenerListaProductos());

        assertEquals(Tipo.RESPUESTA_NO_EXITOSA, ex.getTipo());
    }

    // ---------- helpers ----------

    private ProductListResponse respuesta(String codigo, List<ProductoDto> productos) {
        MensajeDto mensaje = new MensajeDto();
        mensaje.setCodigo(codigo);
        mensaje.setTexto("texto");

        ProductListResponse response = new ProductListResponse();
        response.setMensaje(mensaje);
        response.setProductos(productos);
        return response;
    }

    private ProductoDto producto(int idServicio, int idProducto) {
        ProductoDto dto = new ProductoDto();
        dto.setIdServicio(idServicio);
        dto.setIdProducto(idProducto);
        return dto;
    }

    private RetryableException retryable(Throwable causa) {
        Request request = Request.create(Request.HttpMethod.GET, "http://localhost",
                Collections.emptyMap(), null, StandardCharsets.UTF_8, null);
        return new RetryableException(-1, "error de red", Request.HttpMethod.GET, causa, (Long) null, request);
    }
}