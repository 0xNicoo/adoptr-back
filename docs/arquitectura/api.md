# Contrato HTTP observado — backend

Contrato inferido de los controllers y `SecurityConfig` en esta versión del repositorio; no representa un contrato aprobado ni el resultado de una máquina concreta. Puerto por defecto configurado: 8081. Rutas de documentación: `/swagger-ui/index.html` y `/v3/api-docs`. La configuración OpenAPI anuncia Bearer JWT globalmente, pero `/auth/**` y Swagger están permitidos sin token.

| Método / ruta | Autenticación | Request | Respuesta / límites |
| --- | --- | --- | --- |
| `POST /auth/oauth` | No | JSON `{ "provider": "google", "token": "<Google ID token>" }` | 200 `{ "token": "<JWT Adoptr>", "user": { "id": number, "provider": string, "providerUserId": string, "email": string, "name": string, "profile": null/object, "createdAt": string } }`. No hay validación de input ni control confiable del provider; fallos de validación suelen llegar a 500. |
| `GET /profile` | `Authorization: Bearer <JWT Adoptr>` | Sin cuerpo | **Bloqueado con un JWT emitido por el flujo actual:** el filtro coloca el `sub` (nombre, `String`) como principal y `MembershipSupport` lo castea a `Long`; falla antes de buscar el perfil. `ProfileDTO` declara `id`, `firstName`, `lastName`, `genderType`, `description`, `locality` (esta última es una entidad JPA, no un contrato JSON estabilizado). Solo después de corregir y probar el principal podría alcanzarse el caso «sin perfil», que hoy lanza `BadRequestException` 400 `{ "code":"001", "message":"No se encontro" }`. |
| `POST /profile` | Bearer JWT | Acepta `ProfileDTOin`: `firstName`, `lastName`, `genderType`, `description`, `localityId` | **Stub:** el controller devuelve 200 con cuerpo nulo sin llamar al servicio; no persiste ni valida el DTO. No consumir como operación funcional. |

Reglas globales: cualquier otra ruta (incluso futuras publicaciones) exige autenticación salvo cambio de configuración. Un request sin JWT válido cae en Spring Security; su estado y formato de error no están armonizados explícitamente con `ApiError`. No asumir un 401 ni un JSON uniforme sin una prueba de contrato compartida. El handler MVC global retorna 400 para `BadRequestException`, 401 para `UnauthorizedException` y 500 para excepciones genéricas (`{code:"0", message: mensaje interno}`); no hay mapeo 404 específico. Distinguir errores de filtro/seguridad, JSON/validación de entrada y excepciones del controller: no todos pasan por el mismo handler ni está probada una respuesta uniforme. No reenviar mensajes internos o credenciales en errores nuevos.

### Límite de confianza actual (bloqueo para producción)

`GoogleTokenValidator` comprueba firma y expiración del ID token, **no comprueba `iss` ni `aud`**; `AuthService` usa el `provider` declarado por el cliente para buscar/crear identidad, sin ligarlo al validador Google. La identidad externa no tiene unicidad compuesta explícita en `User`. Además, el secreto de firma JWT estuvo versionado como valor por defecto en `application.properties`. Ahora se exige `SECURITY_JWT_SECRET` externo, pero el valor anterior sigue en el historial: quien lo conozca podría firmar un Bearer si se reutiliza. **No considerar confiables el login ni las rutas protegidas para un despliegue mientras estos riesgos sigan presentes.** Antes de desplegar: externalizar y rotar el secreto (invalidar los JWT firmados con el anterior), validar issuer/audience/proveedor/tiempos y resolver unicidad/concurrencia con pruebas negativas y de integración. Esto describe riesgos del código; no afirma que exista un despliegue.

La respuesta actual de `/auth/oauth` incluye `UserDTO` con email e identificador externo del usuario que inicia sesión; revisar minimización y serialización antes de ampliar consumidores. Esto **no** demuestra exposición pública anónima. Un futuro `PublicationDTO` contiene `UserDTO`: no reutilizarlo como ficha pública sin decidir visibilidad de datos con producto.

**No existen endpoints HTTP** para users, localidades/provincias, publicaciones, mascotas, búsqueda, adopciones, favoritos o chat. `PublicationService.get(Long)` es un método Java interno, no `/publication/{id}`.

## Al evolucionar el contrato (propuesta, no implementada)

Para cada nueva ruta acordar: público vs privado, ownership, JSON/DTO, validaciones, códigos HTTP y error estable, paginación/filtros, compatibilidad con el frontend y tests de contrato. En particular definir si el catálogo de adopción se consulta anónimamente y qué datos de usuario se exponen públicamente; no exponer `UserDTO` completo por defecto. Documentar la decisión aquí antes de darla por existente.
