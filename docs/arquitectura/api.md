# Contrato HTTP observado — backend

Contrato inferido de los controllers y `SecurityConfig` en esta versión del repositorio; no representa un contrato aprobado ni el resultado de una máquina concreta. Puerto por defecto configurado: 8081. Rutas de documentación: `/swagger-ui/index.html` y `/v3/api-docs`. La configuración OpenAPI anuncia Bearer JWT globalmente, pero `/auth/**` y Swagger están permitidos sin token.

| Método / ruta | Autenticación | Request | Respuesta / límites |
| --- | --- | --- | --- |
| `POST /auth/oauth` | No | JSON `{ "provider": "google", "token": "<Google ID token>" }`. `provider` y `token` son obligatorios y no pueden estar en blanco. | 200 `{ "token": "<JWT Adoptr>", "user": { "id": number, "provider": string, "providerUserId": string, "email": string, "name": string, "profile": null/object, "createdAt": string } }`. 400 `VALIDATION_ERROR` si falta un campo y 400 `MALFORMED_REQUEST` si el JSON está mal formado. Un ID token de Google inválido o vencido **todavía llega como 500** (el validador lanza `RuntimeException`); pendiente de mapear a 401. No hay control confiable del `provider`. |
| `GET /provinces` | Bearer JWT | Sin cuerpo | 200 `[{ "id": number, "name": string }]`, las 24 provincias ordenadas por nombre. El `id` es el código oficial de Georef como número (`"06"` → `6`). 401 si el token falta o no es válido. |
| `GET /provinces/{provinceId}/localities?q=texto` | Bearer JWT | `q`: texto a buscar, entre 2 y 100 caracteres. Se ignoran mayúsculas, tildes y espacios de más. | 200 `[{ "id": number, "name": string, "displayName": string }]`, hasta 20 localidades de la provincia cuyo nombre contiene el texto: primero las que empiezan con él y después el resto, por nombre. `id` es el código oficial de Georef como número. `displayName` es el texto a mostrar: igual a `name`, salvo que el nombre se repita en la provincia, en cuyo caso agrega el departamento entre paréntesis (`"San Pedro (Choya)"`). 400 `VALIDATION_ERROR` si `q` falta, es corto o largo, o si `provinceId` no es numérico. 404 `PROVINCE_NOT_FOUND` si la provincia no existe. 401 sin token válido. |
| `GET /profile` | `Authorization: Bearer <JWT Adoptr>` | Sin cuerpo | 200 con `ProfileDTO` del usuario del token: `id`, `firstName`, `lastName`, `genderType`, `description`, `locality` (esta última es una entidad JPA, no un contrato JSON estabilizado: con el catálogo de ubicaciones también expone `displayName`, `searchName`, `departmentName` y `province`; se reemplaza por un DTO en el cambio de alta de perfil). 404 `{ "code":"PROFILE_NOT_FOUND", "message":"No se encontró el perfil" }` si el usuario todavía no tiene perfil. 401 si el token falta o no es válido. |
| `POST /profile` | Bearer JWT | Acepta `ProfileDTOin`: `firstName`, `lastName`, `genderType`, `description`, `localityId` | **Stub:** el controller devuelve 200 con cuerpo nulo sin llamar al servicio; no persiste ni valida el DTO. No consumir como operación funcional. |

## Formato de errores

Todos los errores tienen el cuerpo `{ "code": string, "message": string }`, tanto los del filtro de seguridad como los de los controllers. **`code` es un texto estable y único en toda la API** (`MAYÚSCULAS_CON_GUIONES`): el front decide por `code`, no por `message`, que puede cambiar de redacción. Los errores de un módulo llevan su prefijo (`PROFILE_…`, `PUBLICATION_…`); los de `CommonError` son transversales:

| Estado | `code` | Cuándo |
| --- | --- | --- |
| 401 | `UNAUTHENTICATED` | Token ausente, inválido, vencido o firmado con otro secreto (lo emite `AuthenticationEntryPoint`, no el handler MVC). |
| 403 | `ACCESS_DENIED` | Autenticado sin permiso (lo emite `AccessDeniedHandler`). Hoy ninguna ruta lo produce: no hay roles. |
| 400 | `VALIDATION_ERROR` | Falla de validación de un DTO (el mensaje lista `campo: motivo` de cada campo), de un parámetro de búsqueda o de un parámetro de ruta con formato inválido. |
| 400 | `MALFORMED_REQUEST` | Cuerpo ilegible o JSON mal formado. |
| 404 | `ROUTE_NOT_FOUND` | Ruta inexistente. |
| 405 | `METHOD_NOT_ALLOWED` | Método HTTP no permitido en la ruta. |
| 500 | `INTERNAL_ERROR` | Error inesperado. El mensaje es genérico; el detalle solo va al log del servidor. |

Errores de dominio vigentes: 404 `PROFILE_NOT_FOUND`, 404 `PROVINCE_NOT_FOUND` y 404 `PUBLICATION_NOT_FOUND` (este último sin endpoint que lo exponga todavía). Los recursos de dominio inexistentes responden 404 (`NotFoundException`); reglas de negocio incumplidas responden 400 (`BadRequestException`). Al agregar un error nuevo, usar el prefijo del módulo y registrarlo acá.

Reglas globales: cualquier otra ruta (incluso futuras publicaciones) exige autenticación salvo cambio de configuración. El JWT de Adoptr lleva como `sub` el **id interno del usuario** (numérico) y, como principal, el filtro deja ese mismo `Long`; los servicios lo obtienen con `MembershipSupport`. Los JWT emitidos antes de este cambio (con el nombre como `sub`) dejan de ser válidos. No reenviar mensajes internos o credenciales en errores nuevos.

### Límite de confianza actual (bloqueo para producción)

`GoogleTokenValidator` comprueba firma y expiración del ID token, **no comprueba `iss` ni `aud`**; `AuthService` usa el `provider` declarado por el cliente para buscar/crear identidad, sin ligarlo al validador Google. La identidad externa no tiene unicidad compuesta explícita en `User`. Además, el secreto de firma JWT estuvo versionado como valor por defecto en `application.properties`. Ahora se exige `SECURITY_JWT_SECRET` externo, pero el valor anterior sigue en el historial: quien lo conozca podría firmar un Bearer si se reutiliza. **No considerar confiables el login ni las rutas protegidas para un despliegue mientras estos riesgos sigan presentes.** Antes de desplegar: externalizar y rotar el secreto (invalidar los JWT firmados con el anterior), validar issuer/audience/proveedor/tiempos y resolver unicidad/concurrencia con pruebas negativas y de integración. Esto describe riesgos del código; no afirma que exista un despliegue.

La respuesta actual de `/auth/oauth` incluye `UserDTO` con email e identificador externo del usuario que inicia sesión; revisar minimización y serialización antes de ampliar consumidores. Esto **no** demuestra exposición pública anónima. Un futuro `PublicationDTO` contiene `UserDTO`: no reutilizarlo como ficha pública sin decidir visibilidad de datos con producto.

**No existen endpoints HTTP** para users, publicaciones, mascotas, búsqueda, adopciones, favoritos o chat. `PublicationService.get(Long)` es un método Java interno, no `/publication/{id}`.

## Al evolucionar el contrato (propuesta, no implementada)

Para cada nueva ruta acordar: público vs privado, ownership, JSON/DTO, validaciones, códigos HTTP y error estable, paginación/filtros, compatibilidad con el frontend y tests de contrato. En particular definir si el catálogo de adopción se consulta anónimamente y qué datos de usuario se exponen públicamente; no exponer `UserDTO` completo por defecto. Documentar la decisión aquí antes de darla por existente.
