package com.proyecto.servicios.client;

import com.proyecto.servicios.config.GestoPagoProductClientConfig;
import com.proyecto.servicios.model.gestopago.ProductListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "gestoPagoProducts",
        url = "${gestopago.api.url}",
        configuration = GestoPagoProductClientConfig.class
)
public interface GestoPagoProductClient {

    @GetMapping(value = "/sistema/service/getProductList.do", produces = MediaType.APPLICATION_XML_VALUE)
    ProductListResponse getProductList();
}