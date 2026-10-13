# Integración con GestoPago: getProductList

## Objetivo
Consumir `GET /sistema/service/getProductList.do` del servicio externo PuntoRed/GestoPago para obtener los servicios y productos que el distribuidor puede vender, respetando la estructura de capas del proyecto.

## Arquitectura

| Capa | Clase | Responsabilidad |
|------|-------|-----------------|
| Controller | `GestoPagoProductController` | Expone `GET /gestopago/productos` y devuelve JSON |
| Service | `GestoPagoProductService` / `Impl` | Lógica de negocio, logs, validación y traducción de errores |
| Client | `GestoPagoProductClient` | Cliente Feign hacia el endpoint externo |
| DTOs | `ProductListResponse`, `MensajeDto`, `ProductoDto` | Modelo de la respuesta XML |
| Configuración | `GestoPagoProductClientConfig`, `GestoPagoErrorDecoder` | Bearer Token y decodificación de errores HTTP |
| Excepciones | `GestoPagoException`, `GestoPagoExceptionHandler` | Errores tipados y respuesta uniforme |

## Configuración
Las propiedades siguen el prefijo `gestopago.` ya usado por la autenticación. Todo valor sensible se lee de variables de entorno.

| Propiedad | Variable de entorno |
|-----------|---------------------|
| `gestopago.api.url` | `GESTOPAGO_URL` |
| `gestopago.api.token` | `GESTOPAGO_TOKEN` |

Los timeouts del cliente (`connect-timeout` 5 s y `read-timeout` 62 s) se definen en `application.properties` bajo `spring.cloud.openfeign.client.config.gestoPagoProducts`.

## Decisiones técnicas
1. **Feign** para el cliente, porque el proyecto ya lo usa para la autenticación (`GestoPagoAuthClient`).
2. **Bearer Token en un `RequestInterceptor`**: el token se lee de la configuración y se agrega al header `Authorization`. No está en el código fuente y el service no lo conoce.
3. **XML con `jackson-dataformat-xml`**: el servicio responde XML, no JSON. Los DTOs usan anotaciones `@JacksonXmlProperty` y `ignoreUnknown`, para tolerar campos nuevos.
4. **Config de Feign sin `@Configuration`**, para que el interceptor y el decoder solo apliquen a este cliente y no al de autenticación.
5. **Errores tipados** (`GestoPagoException.Tipo`):
   - `AUTENTICACION`: HTTP 401/403 (token inválido o expirado).
   - `COMUNICACION`: fallas de red.
   - `TIMEOUT`: tiempo de espera agotado.
   - `RESPUESTA_NO_EXITOSA`: otros HTTP no exitosos o código distinto de `01` en el XML.
6. **Mapeo HTTP en `@RestControllerAdvice`**: TIMEOUT a 504, COMUNICACION a 503, el resto a 502.
7. **Logs**: se registra inicio y fin de la invocación y los errores con tipo y estatus. Nunca se registra el token ni el cuerpo de la respuesta.
8. **Inyección por constructor** en service, controller y cliente.

## Consideraciones del servicio externo
- El endpoint solo debe invocarse hasta 3 veces al día, o se bloquea la IP. Por eso las pruebas usan mocks.
- El token JWT de GestoPago dura 24 horas; el proyecto ya lo renueva con `GestoPagoTokenServiceImpl`.

## Pruebas unitarias
`GestoPagoProductServiceImplTest` (JUnit 5 + Mockito) cubre: respuesta exitosa, código no exitoso, respuesta nula, respuesta sin mensaje, timeout, error de comunicación, error de autenticación y error HTTP genérico.

```
.\gradlew.bat test --tests "*GestoPagoProductServiceImplTest" --no-daemon
```