package com.proyecto.servicios.model.gestopago;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductoDto {

    @JacksonXmlProperty(isAttribute = true)
    private String servicio;

    @JacksonXmlProperty(isAttribute = true)
    private String producto;

    @JacksonXmlProperty(isAttribute = true)
    private Integer idServicio;

    @JacksonXmlProperty(isAttribute = true)
    private Integer idProducto;

    @JacksonXmlProperty(isAttribute = true)
    private Integer idCatTipoServicio;

    @JacksonXmlProperty(isAttribute = true)
    private Integer tipoFront;

    @JacksonXmlProperty(isAttribute = true)
    private Boolean hasDigitoVerificador;

    @JacksonXmlProperty(isAttribute = true)
    private BigDecimal precio;

    @JacksonXmlProperty(isAttribute = true)
    private Boolean showAyuda;

    @JacksonXmlProperty(isAttribute = true)
    private String tipoReferencia;

    @JacksonXmlProperty(localName = "legend")
    private String legend;
}