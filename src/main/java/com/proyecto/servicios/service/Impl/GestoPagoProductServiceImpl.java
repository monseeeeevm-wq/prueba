package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.client.GestoPagoProductClient;
import com.proyecto.servicios.exception.GestoPagoException;
import com.proyecto.servicios.exception.GestoPagoException.Tipo;
import com.proyecto.servicios.model.gestopago.ProductListResponse;
import com.proyecto.servicios.service.GestoPagoProductService;
import feign.FeignException;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.SocketTimeoutException;

@Service
@Slf4j
public class GestoPagoProductServiceImpl implements GestoPagoProductService {

    private static final String CODIGO_EXITO = "01";

    private final GestoPagoProductClient productClient;

    public GestoPagoProductServiceImpl(GestoPagoProductClient productClient) {
        this.productClient = productClient;
    }

    @Override
    public ProductListResponse obtenerListaProductos() {
        log.info("Inicio de invocacion a GestoPago getProductList");
        try {
            ProductListResponse response = productClient.getProductList();
            validarRespuesta(response);
            log.info("Fin de invocacion a GestoPago getProductList. productos={}",
                    response.getProductos() == null ? 0 : response.getProductos().size());
            return response;
        } catch (GestoPagoException e) {
            log.error("Error de integracion GestoPago. tipo={}, status={}", e.getTipo(), e.getStatus());
            throw e;
        } catch (RetryableException e) {
            throw traducirErrorDeRed(e);
        } catch (FeignException e) {
            log.error("Error al procesar respuesta de GestoPago. status={}", e.status());
            throw new GestoPagoException(Tipo.RESPUESTA_NO_EXITOSA,
                    "No se pudo procesar la respuesta de GestoPago", e);
        }
    }

    private void validarRespuesta(ProductListResponse response) {
        if (response == null || response.getMensaje() == null
                || !CODIGO_EXITO.equals(response.getMensaje().getCodigo())) {
            String codigo = (response == null || response.getMensaje() == null)
                    ? "sin-mensaje" : response.getMensaje().getCodigo();
            log.error("GestoPago respondio sin codigo de exito. codigo={}", codigo);
            throw new GestoPagoException(Tipo.RESPUESTA_NO_EXITOSA,
                    "GestoPago respondio con codigo no exitoso: " + codigo, 200);
        }
    }

    private GestoPagoException traducirErrorDeRed(RetryableException e) {
        if (e.getCause() instanceof SocketTimeoutException) {
            log.error("Timeout al comunicarse con GestoPago");
            return new GestoPagoException(Tipo.TIMEOUT, "Tiempo de espera agotado con GestoPago", e);
        }
        log.error("Error de comunicacion con GestoPago");
        return new GestoPagoException(Tipo.COMUNICACION, "No fue posible comunicarse con GestoPago", e);
    }
}