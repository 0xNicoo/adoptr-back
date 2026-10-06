# Arquitectura y lineamientos — backend

Descripción de la estructura del repositorio y criterios para evolucionarla; actualizarla al cambiar las capas. Los endpoints se documentan en [API](api.md) y el alcance de negocio en [dominio](../producto/dominio-y-alcance.md). Validar contratos con la [integración frontend](../../../adoptr-front/docs/arquitectura/integracion.md).

## Componentes y responsabilidades

- Stack: Java 25, Spring Boot 4, Gradle, Spring MVC/Security/Data JPA, PostgreSQL, MapStruct y Lombok. Paquetes por capacidad: `auth`, `user`, `profile`, `location`, `publication`; `shared` contiene seguridad, configuración, errores y utilidades transversales.
- Flujo HTTP: controller (borde/DTO) → service (casos de uso) → repository (JPA) → PostgreSQL. `auth` valida el ID token Google y emite un JWT propio. El filtro JWT de `shared/security` procesa las rutas protegidas antes de llegar a controllers.
- Relaciones principales: `User` ↔ `Profile` (1:1), `Locality` → `Province` (N:1), `Publication` → `User`/`Locality` (N:1; usuario obligatorio, localidad opcional). `Publication` usa herencia JPA JOINED y `PublicationType`; el modelo todavía no representa mascota, estado ni solicitud. Ver [dominio](../producto/dominio-y-alcance.md) para las decisiones de producto aún abiertas.
- **Cobertura efectiva:** solo hay controllers HTTP de `auth` y `profile`; la existencia de service, DTO o entidad no constituye una API. `POST /profile` es stub y `GET /profile` falla con el JWT actual antes de buscar en la base; ver [API](api.md). La única prueba encontrada en `src/test` carga el contexto, no valida contratos o permisos.

## Criterios de diseño

- Mantener capas por capacidad: controller para HTTP, validación y DTO; service para invariantes, permisos y transacciones; repository solo para persistencia. Exponer DTO, no entidades JPA ni grafos completos por defecto.
- Autorizar operaciones sobre recursos por identidad y dueño, además de verificar el JWT. Un proveedor declarado por el cliente no es fuente de confianza; validar issuer y audience de tokens externos. No usar nombres como identificador interno estable: actualmente el JWT lleva `sub` = nombre y un claim `id`, pero el filtro establece el `sub` (`String`) como principal y `MembershipSupport` espera `Long`. Elegir un ID interno único para principal/servicios y cubrirlo con una prueba de ruta real.
- Definir transiciones de publicación, fotos y solicitudes con el equipo antes de ampliar el modelo. Elegir herencia solo si hay invariantes compartidas; evitar inferirla de los valores actuales del enum.
- Mantener IDs de localidad estables en la API y desacoplar DTO de entidades: `ProfileDTO.locality` hoy declara una entidad JPA y `PublicationDTO.user` declara `UserDTO`; no usarlos como contrato público sin revisar forma JSON, ciclos y privacidad. Mover secretos fuera del repositorio, controlar cambios del esquema con migraciones versionadas y alinear errores HTTP con contratos probados. Garantizar unicidad de identidad externa (`provider`, ID externo) y probar altas concurrentes antes de depender de esa búsqueda.

## Ejecución compartida y contenedores

- **Implementado:** `Dockerfile` multietapa (JDK/JRE Java 25), ejecución sin privilegios, `.dockerignore` con lista de archivos permitidos y `.env.example` sin secretos. Construye `bootJar` sin ejecutar tests; estos requieren PostgreSQL y se ejecutan aparte. Puerto `${PORT:8081}` y credenciales/JWT obligatorios por entorno. Ver comandos de ejecución local en [README](../../README.md). `run.sh` selecciona `.env.local` (PostgreSQL del host en Linux con red host) o `.env.develop` (base remota, red bridge) y construye/ejecuta la imagen localmente. Ambos archivos son privados; los ejemplos se versionan. No incluye healthcheck HTTP dedicado.
- **Implementado para desarrollo local del backend:** `compose.yaml` reutiliza el Dockerfile y levanta PostgreSQL 17 en otro servicio, con volumen persistente, sin puerto de base publicado y con `pg_isready` antes del arranque del backend. Usa `.env.local` para credenciales/JWT, pero reemplaza la URL JDBC por el servicio `db`. Funciona con Compose v2 y contenedores Linux en Windows/Linux. Esa base es independiente de cualquier PostgreSQL instalado en el host.
- **Pendiente:** un orquestador que incluya ambos repositorios (frontend/backend) debería tener un dueño acordado o ubicación consensuada, no duplicarse en cada aplicación. Compose no sustituye tests, configuración ni decisiones de negocio; su integración en CI sigue pendiente.
- Diseñar backend y base como servicios distintos: imagen de build/runtime Java 25, URL JDBC hacia el nombre de servicio de PostgreSQL, variables externas para credenciales/JWT, volumen persistente para datos de desarrollo y healthchecks antes de pruebas de integración. No poner contraseñas en Dockerfile, imagen ni Compose versionado. El puerto 8081 publicado solo si hace falta acceder desde el host.
- Para despliegue, construir imágenes inmutables y usar PostgreSQL gestionado o persistencia/backup explícito; introducir migraciones antes de reemplazar `ddl-auto=update`. Contenerizar **no corrige** por sí solo el validador de Google, permisos ni secretos ya versionados.

## API y pruebas

- Definir DTO de request/response explícitos y mantener compatibilidad deliberada; acordar status/errores, paginación y filtros para listados. Evitar serialización JPA implícita, N+1 y exposición innecesaria de datos del usuario.
- Cubrir validadores, servicios, repositorios y rutas/permisos con pruebas automáticas; usar PostgreSQL de prueba reproducible y `./gradlew test` en CI. Revisar CORS, credenciales y permisos por entorno.

## Prioridades técnicas a resolver

1. Externalizar y rotar secretos versionados; validar `iss`/`aud`/vigencia del token Google y no confiar en el `provider` del request. Fijar el principal JWT como ID interno consistente entre filtro y servicios.
2. Completar creación/lectura de perfiles con DTO, validación, permisos y localidades; armonizar códigos de error y asegurar unicidad de identidad externa.
3. Acordar con producto los datos, estados, imágenes y privacidad de la publicación de adopción; implementar creación, consulta y solicitud/contacto solo después de cerrar contratos con frontend.
4. Introducir migraciones, pruebas de seguridad/contrato con PostgreSQL y CI; revisar `@Data` en relaciones bidireccionales, N+1 y exposición de datos.

## Criterios de habilitación (propuesta, no estado implementado)

- **Antes de confiar en auth o desplegar:** mantener configuración externa obligatoria (los secretos por defecto ya se retiraron), rotar el secreto JWT conocido e invalidar tokens anteriores; exigir configuración externa por entorno. Validar firma, issuer, audience, vigencia y proveedor de identidad con casos negativos; asegurar unicidad de identidad externa. Hoy tampoco hay pruebas de ese límite de confianza. Revisar CORS (actualmente permite `http://localhost:3000`) y Swagger (autorización Bearer global documentada aunque auth es pública) por entorno.
- **Antes de publicar un endpoint protegido:** usar el mismo ID interno en JWT, filtro y servicios; probar JWT válido, ausente, inválido y vencido, usuario ajeno, request inválido y errores sin mensajes internos. La anotación `authenticated()` no autoriza la propiedad de un recurso. Separar errores de Spring Security de los del handler MVC hasta unificarlos con pruebas.
- **Antes de cambiar esquema/activar despliegue:** migraciones versionadas sobre PostgreSQL reproducible, restricciones/índices y rollback/backup acordados; `ddl-auto=update` solo sirve para desarrollo. La imagen Docker y Compose local con healthcheck PostgreSQL están definidos en el repositorio; el healthcheck HTTP del backend y la integración CI siguen pendientes.
- **Antes de afirmar feature terminada:** contrato de [API](api.md) sincronizado con [integración frontend](../../../adoptr-front/docs/arquitectura/integracion.md), DTO mínimo, persistencia, autorización, validaciones y pruebas HTTP/servicio. Definir con [producto](../producto/dominio-y-alcance.md) las reglas no decididas antes de implementarlas.

Mantener la lista de prioridades corta y orientada a decisiones del código, no a avances de un entorno particular.
