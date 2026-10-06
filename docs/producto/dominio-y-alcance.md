# Dominio y alcance — backend

Vocabulario de trabajo para consensuar requisitos con el equipo. No confundir modelos técnicos (ver [arquitectura](../arquitectura/lineamientos.md)) con reglas de negocio aprobadas ni asumir avances de otras ramas.

## Conceptos de dominio

- **Usuario/perfil:** identidad y datos de la persona; por definir si hay roles o verificaciones para particulares y refugios.
- **Responsable:** persona u organización que publica y gestiona consultas sobre una mascota; por definir si puede transferir titularidad o colaborar con otros usuarios.
- **Publicación de adopción:** anuncio de una mascota con ubicación, información y estado; por definir atributos obligatorios, imágenes, visibilidad y transiciones.
- **Persona interesada/solicitud:** quien expresa interés en adoptar; por definir identidad requerida, datos compartidos, canal de contacto y resolución de la solicitud.
- **Ubicación:** provincia/localidad para búsqueda y contacto; por definir granularidad pública y fuente de datos.

`LOST` y `SERVICE` son otras categorías contempladas por el modelo de publicaciones, pero su alcance respecto del MVP debe definirse explícitamente.

## Estado observado del código (no requisitos aprobados)

| Pieza | Lo que existe | Lo que **no** se puede inferir |
| --- | --- | --- |
| Identidad | `User` guarda proveedor, ID externo, email y nombre; `POST /auth/oauth` busca o crea el usuario y emite JWT. | Google como única opción futura, cuentas verificadas, vinculación de cuentas o autorización para publicar. La validación de identidad actual tiene riesgos; ver [API](../arquitectura/api.md). |
| Perfil y ubicación | `Profile` se relaciona 1:1 con `User` y N:1 con `Locality`; `Locality` pertenece a `Province`. El enum `Gender` (`FEMALE`, `MALE`) pertenece al **perfil humano**, no a la mascota. | Alta de perfil operativa: `POST /profile` no persiste y `GET /profile` falla con el JWT actual. Tampoco hay catálogo HTTP de localidades. |
| Publicación | Entidad `Publication` con título, descripción, fecha, usuario obligatorio, `imageId` singular, localidad opcional y tipo `ADOPTION`, `LOST` o `SERVICE`; usa herencia JPA `JOINED`. | Ficha de mascota, fotos operativas, estado/visibilidad, subtipos concretos, autorización de dueño ni endpoint de publicación. `imageId` no define almacenamiento ni política de imágenes. |
| Adopción | No hay entidad ni endpoint para solicitud, conversación o resultado. | No hay recorrido de adopción implementado por tener una entidad `Publication` o un servicio interno de lectura por ID. |

Fuentes de este inventario: `src/main/java/com/adoptr/adoptr/{user,profile,location,publication}/model/`, `auth/service/AuthService.java`, `profile/controller/ProfileController.java` y [contrato HTTP observado](../arquitectura/api.md). Revisarlo al cambiar código: un enum, DTO o asociación JPA no aprueba una regla de negocio.

## Recorrido de producto a definir con el equipo

Como hipótesis para un MVP (no contrato aprobado): una persona responsable publica una mascota; visitantes la descubren y consultan; alguien interesado inicia una solicitud; la persona responsable responde y se registra un resultado. Antes de construirlo, cerrar estas preguntas:

1. ¿Quiénes pueden publicar: particulares, refugios o ambos? ¿Requiere verificación? ¿Pueden consultar el catálogo personas no autenticadas?
2. ¿Qué atributos de la mascota y fotos son obligatorios? ¿Quién almacena las imágenes y por cuánto tiempo? ¿Qué datos personales pueden mostrarse públicamente?
3. ¿Qué estados necesita la publicación (borrador, activa, pausada, adoptada, etc.) y quién autoriza cada transición? ¿Cómo se evita recibir solicitudes de una publicación cerrada?
4. ¿Qué es una solicitud de adopción: formulario, contacto externo o conversación? ¿Quién la ve, cómo se notifica, se modera y se cierra?
5. ¿Qué ubicación mostrar y con qué precisión? ¿Qué políticas aplican a animales perdidos y servicios si se incorporan al alcance?

## Decisiones que habilitan implementación (pendientes)

- **Identidad y acceso:** acordar proveedores admitidos, uso de email verificado, duplicados/vinculación de cuentas y si la lectura del catálogo será anónima. La configuración actual exige JWT para toda ruta futura excepto `/auth/**` y Swagger: habilitar catálogo público requeriría cambiarla y probarlo; no basta con dibujar una pantalla pública.
- **Perfil y datos personales:** acordar cuándo se exige completar perfil, qué campos son necesarios, quién los puede consultar y si la localidad del perfil difiere de la de la publicación. Decidir fuente/actualización de provincias y localidades y precisión de la ubicación mostrada; no copiar datos de contacto al DTO público por comodidad.
- **Publicación y titularidad:** decidir datos de mascota, imágenes (almacenamiento, consentimiento y retención), quién puede crear/editar/cerrar y qué estados/transiciones existen. La relación `Publication.user` no verifica permisos por sí sola. Definir qué pasa con consultas pendientes al cerrar o adoptar.
- **Interés y resolución:** decidir si la interacción es formulario, contacto externo o mensajería; quién ve qué datos, notificaciones, moderación, consentimiento, retención y resultado. No crear estados ni endpoints de solicitud antes de acordarlo.
- **Otras categorías:** decidir explícitamente si `LOST` y `SERVICE` se implementan en el MVP o después; que aparezcan en `PublicationType` no significa que haya flujos disponibles.

Registrar cada acuerdo con **decisión, responsable/fecha, alcance, casos límite, datos visibles y criterio de aceptación**; hasta entonces mantenerlo como pregunta, no como contrato. Antes de declarar funcional un recorrido exigir endpoint y persistencia reales, permisos por dueño, validaciones, DTO público mínimo, errores y pruebas de éxito/rechazo/transición/privacidad, más UI integrada con estados. Hoy solo hay prueba de carga de contexto en backend. Referencias: [arquitectura](../arquitectura/lineamientos.md), [contrato API](../arquitectura/api.md) y [flujos frontend](../../../adoptr-front/docs/producto/flujos-producto.md).
