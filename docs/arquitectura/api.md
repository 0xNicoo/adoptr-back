# Contrato HTTP observado — backend

Contrato inferido de los controllers y `SecurityConfig` en esta versión del repositorio; no representa un contrato aprobado ni el resultado de una máquina concreta. Puerto por defecto configurado: 8081. Rutas de documentación: `/swagger-ui/index.html` y `/v3/api-docs`. La configuración OpenAPI anuncia Bearer JWT globalmente, pero `/auth/**`, Swagger y las lecturas de provincias y localidades están permitidos sin token.

| Método / ruta | Autenticación | Request | Respuesta / límites |
| --- | --- | --- | --- |
| `POST /auth/oauth` | No | JSON `{ "provider": "google", "token": "<Google ID token>" }`. `provider` y `token` son obligatorios y no pueden estar en blanco. | 200 `{ "token": "<JWT Adoptr>", "user": { "id": number, "provider": string, "providerUserId": string, "email": string, "name": string, "profile": null/object, "createdAt": string } }`. 400 `VALIDATION_ERROR` si falta un campo y 400 `MALFORMED_REQUEST` si el JSON está mal formado. Un ID token de Google inválido o vencido **todavía llega como 500** (el validador lanza `RuntimeException`); pendiente de mapear a 401. No hay control confiable del `provider`. |
| `GET /provinces` | No (pública) | Sin cuerpo | 200 `[{ "id": number, "name": string }]`, las 24 provincias ordenadas por nombre. El `id` es el código oficial de Georef como número (`"06"` → `6`). Un header `Authorization` inválido se ignora: la ruta no responde 401. |
| `GET /provinces/{provinceId}/localities?q=texto` | No (pública) | `q`: texto a buscar, entre 2 y 100 caracteres. Se ignoran mayúsculas, tildes y espacios de más. | 200 `[{ "id": number, "name": string, "displayName": string }]`, hasta 20 localidades de la provincia cuyo nombre contiene el texto: primero las que empiezan con él y después el resto, por nombre. `id` es el código oficial de Georef como número. `displayName` es el texto a mostrar: igual a `name`, salvo que el nombre se repita en la provincia, en cuyo caso agrega el departamento entre paréntesis (`"San Pedro (Choya)"`). 400 `VALIDATION_ERROR` si `q` falta, es corto o largo, o si `provinceId` no es numérico. 404 `PROVINCE_NOT_FOUND` si la provincia no existe. Un header `Authorization` inválido se ignora. |
| `GET /profile` | `Authorization: Bearer <JWT Adoptr>` | Sin cuerpo | 200 con el `ProfileDTO` del usuario del token (forma abajo). 404 `{ "code":"PROFILE_NOT_FOUND", "message":"No se encontró el perfil" }` si el usuario todavía no tiene perfil. 401 si el token falta o no es válido. |
| `POST /profile` | Bearer JWT | `ProfileDTOin` (abajo). El dueño es siempre el usuario del token. | 201 con el `ProfileDTO` creado. 400 `VALIDATION_ERROR` si falta un campo obligatorio o un dato es inválido (el mensaje lista `campo: motivo`). 400 `MALFORMED_REQUEST` si `profileType` o `genderType` no son valores válidos. 400 `LOCALITY_NOT_FOUND` si `localityId` no existe. 409 `PROFILE_ALREADY_EXISTS` si el usuario ya tiene perfil (editarlo será otra operación). 401 sin token válido. |
| `POST /adoptions` | Bearer JWT y perfil creado | `AdoptionDTOin` (abajo). El autor es siempre el usuario del token. | 201 `AdoptionDTO` con `status: "AVAILABLE"`. 400 `VALIDATION_ERROR` (campo faltante o en blanco, largo excedido, tamaño que no corresponde a la especie, fecha futura o de hace más de 30 años); 400 `MALFORMED_REQUEST` (JSON mal formado, enum inexistente, fecha que no es `aaaa-mm`); 400 `LOCALITY_NOT_FOUND`; 403 `PROFILE_REQUIRED` si el usuario no tiene perfil; 401 sin token válido. |
| `GET /adoptions/{id}` | No (pública) | Sin cuerpo. Un Bearer válido es opcional y solo cambia `viewerIsOwner`; uno inválido o vencido se ignora. | 200 `AdoptionDTO`, también si está `ADOPTED`, con `Cache-Control: private`. 404 `ADOPTION_NOT_FOUND` si no existe o fue dada de baja. |
| `PUT /adoptions/{id}` | Bearer JWT, solo quien publicó | `AdoptionDTOin`: reemplaza la ficha completa. No cambia estado, autor ni fecha de alta. | 200 `AdoptionDTO`. 404 `ADOPTION_NOT_FOUND`, después 403 `ADOPTION_NOT_OWNER`, después los 400 de `POST`. Un body con campos faltantes da 400 antes de chequear el dueño. 401 sin token válido. |
| `PATCH /adoptions/{id}/status` | Bearer JWT, solo quien publicó | `{ "status": "AVAILABLE" \| "ADOPTED" }` | 200 `AdoptionDTO`. Cualquier transición entre los dos estados; si ya tenía ese estado devuelve 200 sin cambiar `updatedAt`. 400 `VALIDATION_ERROR` sin `status`, 400 `MALFORMED_REQUEST` con un estado inexistente, 404 `ADOPTION_NOT_FOUND`, 403 `ADOPTION_NOT_OWNER`, 401 sin token. |
| `DELETE /adoptions/{id}` | Bearer JWT, solo quien publicó | Sin cuerpo | 204. Baja lógica: deja de aparecer en el detalle (404) y en `/adoptions/mine`; repetirla da 404. 403 `ADOPTION_NOT_OWNER`, 401 sin token. |
| `GET /adoptions/mine?page=0&pageSize=20` | Bearer JWT | `page` desde 0 (por defecto 0); `pageSize` de 1 a 50 (por defecto 20). | 200 `Page<AdoptionSummaryDTO>` con todas las publicaciones del usuario del token, en cualquier estado y sin las dadas de baja, de la más nueva a la más vieja. 400 `VALIDATION_ERROR` si `page` o `pageSize` están fuera de rango. 401 sin token. |

### Perfil

Hay un perfil por usuario, de tipo `PERSON` u `ORGANIZATION`. En los dos casos se puede adoptar y publicar: solo cambian los datos.

**`ProfileDTOin`** (request de `POST /profile`). Los textos se recortan; un texto en blanco cuenta como ausente. Los campos del otro tipo se ignoran y no se guardan.

| Campo | Tipo | Obligatorio | Límite |
| --- | --- | --- | --- |
| `profileType` | `"PERSON"` \| `"ORGANIZATION"` | Siempre | |
| `description` | string | Siempre | 500 |
| `localityId` | number (id de Georef) | Siempre | Debe existir |
| `firstName`, `lastName` | string | Persona | 100 c/u |
| `genderType` | `"FEMALE"` \| `"MALE"` \| `"X"` | Persona | |
| `organizationName` | string | Organización | 150 |
| `instagram`, `facebook` | string libre (usuario, link o nombre de página) | No | 100 c/u |
| `website` | string, empieza con `http://` o `https://` | No | 255 |

**`ProfileDTO`** (respuesta de `GET` y `POST /profile`, y `user.profile` en `POST /auth/oauth`, que es `null` si el usuario no tiene perfil):

```json
{
  "id": 1,
  "profileType": "PERSON",
  "description": "string",
  "locality": { "id": 6441030, "name": "La Plata", "displayName": "La Plata" },
  "province": { "id": 6, "name": "Buenos Aires" },
  "firstName": "string", "lastName": "string", "genderType": "X",
  "organizationName": null, "instagram": null, "facebook": null, "website": null
}
```

Los campos de persona vienen en `null` en un perfil de organización y viceversa: el front decide por `profileType`.

### Publicación de adopción

Decisiones de producto en [dominio](../producto/dominio-y-alcance.md). Fechas con hora en ISO local sin zona (hora de Argentina), por ejemplo `"2026-11-07T12:00:00"`.

**`AdoptionDTOin`** (request de `POST` y `PUT /adoptions/{id}`). `petName` y `description` se recortan; en blanco cuentan como ausentes.

| Campo | Tipo | Obligatorio | Regla |
| --- | --- | --- | --- |
| `petName` | string | Sí | Hasta 50 caracteres |
| `description` | string | Sí | Hasta 2000 caracteres |
| `species` | `"DOG"` \| `"CAT"` | Sí | |
| `sex` | `"MALE"` \| `"FEMALE"` \| `"UNKNOWN"` | Sí | |
| `size` | `"SMALL"` \| `"MEDIUM"` \| `"LARGE"` \| `null` | Solo perros | Obligatorio si es perro; debe ser `null` o faltar si es gato |
| `estimatedBirthDate` | string `"aaaa-mm"` | Sí | No futura ni de hace más de 30 años (el mes actual vale) |
| `vaccinated`, `dewormed`, `neutered` | boolean | Sí | Solo sí o no: si no se sabe, se envía `false` |
| `localityId` | number (id de Georef) | Sí | Debe existir |

**`AdoptionDTO`** (detalle; respuesta de `POST`, `GET /{id}`, `PUT` y `PATCH`):

```json
{
  "id": 12,
  "petName": "Luna",
  "description": "Muy cariñosa.",
  "species": "DOG",
  "sex": "FEMALE",
  "size": "MEDIUM",
  "estimatedBirthDate": "2024-03",
  "lifeStage": "ADULT",
  "vaccinated": true,
  "dewormed": true,
  "neutered": false,
  "status": "AVAILABLE",
  "locality": { "id": 6441030, "name": "La Plata", "displayName": "La Plata" },
  "province": { "id": 6, "name": "Buenos Aires" },
  "owner": { "displayName": "Refugio Patitas", "profileType": "ORGANIZATION" },
  "viewerIsOwner": false,
  "createdAt": "2026-11-07T12:00:00",
  "updatedAt": "2026-11-07T12:00:00"
}
```

- `lifeStage` se calcula con los meses cumplidos hasta el mes actual: `PUPPY` menos de 12, `ADULT` de 12 a menos de 96 (8 años), `SENIOR` 96 o más.
- `owner` es el responsable: nombre y apellido de una persona o nombre de la organización, más el tipo de perfil. Nunca incluye email, ids de proveedor ni el id interno del usuario.
- `viewerIsOwner` es `true` solo si el request trae un JWT válido de quien publicó. Sirve para que la interfaz muestre «Editar», «Marcar como adoptada» o «Dar de baja»; los permisos reales los verifica el backend en cada operación. Como la respuesta cambia según quién mira, el detalle sale con `Cache-Control: private` y **no debe cachearse compartido entre usuarios**.

**`AdoptionSummaryDTO`** (tarjeta de listado): los mismos campos que `AdoptionDTO` sin `description`, `owner` ni `viewerIsOwner`.

**`Page<T>`** (listados paginados):

```json
{ "items": [], "page": 0, "pageSize": 20, "totalItems": 134, "totalPages": 7 }
```

## Formato de errores

Todos los errores tienen el cuerpo `{ "code": string, "message": string }`, tanto los del filtro de seguridad como los de los controllers. **`code` es un texto estable y único en toda la API** (`MAYÚSCULAS_CON_GUIONES`): el front decide por `code`, no por `message`, que puede cambiar de redacción. Los errores de un módulo llevan su prefijo (`PROFILE_…`, `PUBLICATION_…`); los de `CommonError` son transversales:

| Estado | `code` | Cuándo |
| --- | --- | --- |
| 401 | `UNAUTHENTICATED` | Token ausente, inválido, vencido o firmado con otro secreto (lo emite `AuthenticationEntryPoint`, no el handler MVC). |
| 403 | `ACCESS_DENIED` | Autenticado sin permiso (lo emite `AccessDeniedHandler`). Hoy ninguna ruta lo produce: no hay roles. Los 403 de reglas de negocio llevan su propio `code` (ver abajo). |
| 400 | `VALIDATION_ERROR` | Falla de validación de un DTO (el mensaje lista `campo: motivo` de cada campo), de un parámetro de búsqueda o de un parámetro de ruta con formato inválido. |
| 400 | `MALFORMED_REQUEST` | Cuerpo ilegible o JSON mal formado. |
| 404 | `ROUTE_NOT_FOUND` | Ruta inexistente. |
| 405 | `METHOD_NOT_ALLOWED` | Método HTTP no permitido en la ruta. |
| 500 | `INTERNAL_ERROR` | Error inesperado. El mensaje es genérico; el detalle solo va al log del servidor. |

Errores de dominio vigentes: 404 `PROFILE_NOT_FOUND`, 409 `PROFILE_ALREADY_EXISTS`, 403 `PROFILE_REQUIRED` (la acción exige tener perfil, por ejemplo publicar), 404 `PROVINCE_NOT_FOUND`, 400 `LOCALITY_NOT_FOUND` (una localidad inexistente enviada en un body), 404 `ADOPTION_NOT_FOUND` (no existe o fue dada de baja), 403 `ADOPTION_NOT_OWNER` (la publicación es de otra persona) y 404 `PUBLICATION_NOT_FOUND` (sin endpoint que lo exponga todavía). Los recursos de dominio inexistentes responden 404 (`NotFoundException`); reglas de negocio incumplidas o referencias inválidas en el body responden 400 (`BadRequestException`); un choque con el estado actual, como un recurso que ya existe, responde 409 (`ConflictException`); una operación que la persona autenticada no puede hacer responde 403 (`ForbiddenException`). Al agregar un error nuevo, usar el prefijo del módulo y registrarlo acá.

Reglas globales: son públicas `/auth/**`, Swagger las lecturas `GET /provinces` y `GET /provinces/**` (catálogo oficial sin datos personales) y el detalle `GET /adoptions/{id}`; `GET /adoptions/mine` requiere token (la regla está antes que la de `{id}`); cualquier otra ruta, u otro método sobre esas rutas, exige autenticación salvo cambio de configuración. El JWT de Adoptr lleva como `sub` el **id interno del usuario** (numérico) y, como principal, el filtro deja ese mismo `Long`; los servicios lo obtienen con `MembershipSupport`. Los JWT emitidos antes de este cambio (con el nombre como `sub`) dejan de ser válidos. No reenviar mensajes internos o credenciales en errores nuevos.

### Límite de confianza actual (bloqueo para producción)

`GoogleTokenValidator` comprueba firma y expiración del ID token, **no comprueba `iss` ni `aud`**; `AuthService` usa el `provider` declarado por el cliente para buscar/crear identidad, sin ligarlo al validador Google. La identidad externa no tiene unicidad compuesta explícita en `User`. Además, el secreto de firma JWT estuvo versionado como valor por defecto en `application.properties`. Ahora se exige `SECURITY_JWT_SECRET` externo, pero el valor anterior sigue en el historial: quien lo conozca podría firmar un Bearer si se reutiliza. **No considerar confiables el login ni las rutas protegidas para un despliegue mientras estos riesgos sigan presentes.** Antes de desplegar: externalizar y rotar el secreto (invalidar los JWT firmados con el anterior), validar issuer/audience/proveedor/tiempos y resolver unicidad/concurrencia con pruebas negativas y de integración. Esto describe riesgos del código; no afirma que exista un despliegue.

La respuesta actual de `/auth/oauth` incluye `UserDTO` con email e identificador externo del usuario que inicia sesión; revisar minimización y serialización antes de ampliar consumidores. Esto **no** demuestra exposición pública anónima.

**No existen endpoints HTTP** para users, catálogo o búsqueda de adopciones (llega en el PR siguiente), solicitudes de adopción, perdidas, servicios, favoritos o chat. `PublicationService.get(Long)` es un método Java interno, no `/publication/{id}`.

## Al evolucionar el contrato (propuesta, no implementada)

Para cada nueva ruta acordar: público vs privado, ownership, JSON/DTO, validaciones, códigos HTTP y error estable, paginación/filtros, compatibilidad con el frontend y tests de contrato. No exponer `UserDTO` completo por defecto: el responsable de una publicación se expone solo como `owner` (nombre para mostrar y tipo de perfil). Documentar la decisión aquí antes de darla por existente.
