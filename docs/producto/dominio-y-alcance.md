# Dominio y alcance — backend

Vocabulario de trabajo para consensuar requisitos con el equipo. No confundir modelos técnicos (ver [arquitectura](../arquitectura/lineamientos.md)) con reglas de negocio aprobadas ni asumir avances de otras ramas.

## Conceptos de dominio

- **Usuario/perfil:** identidad y datos de quien usa la plataforma. Tipos de perfil decididos (ver abajo); por definir si hay verificaciones para refugios.
- **Responsable:** persona u organización que publica y gestiona consultas sobre una mascota; por definir si puede transferir titularidad o colaborar con otros usuarios.
- **Publicación de adopción:** anuncio de una mascota con ubicación, información y estado; por definir atributos obligatorios, imágenes, visibilidad y transiciones.
- **Persona interesada/solicitud:** quien expresa interés en adoptar; por definir identidad requerida, datos compartidos, canal de contacto y resolución de la solicitud.
- **Ubicación:** provincia/localidad para búsqueda y contacto; por definir la granularidad pública. Fuente de datos decidida (ver abajo).

### Decisión: catálogo de provincias y localidades (Dolores, 2026-11-05)

- **Fuente:** Servicio Georef (datos.gob.ar), elaborado con datos de IGN, BAHRA e INDEC. Se guarda una copia en el repositorio (`src/main/resources/data/georef/locations.json`, descargada el 2026-11-05) y el backend la importa al arrancar si la tabla de provincias está vacía. No se consulta Georef en tiempo de ejecución.
- **Licencia:** CC BY 4.0. **Toda interfaz que muestre provincias o localidades debe citar la fuente** como «Servicio Georef – argentina.gob.ar/georef» o con un enlace a ese sitio.
- **Alcance:** las 24 provincias y las localidades simples y componentes de localidades compuestas. Se descartan las «Entidad» (subdivisiones de otra localidad, que duplicaban nombres como Mar del Plata), salvo en CABA, que se ofrece por sus 48 barrios más «Ciudad de Buenos Aires». Total: 3.398 localidades.
- **Ids:** el código oficial de Georef como número; no cambia al reimportar.
- **Nombres repetidos:** hay pueblos distintos con el mismo nombre en una provincia (por ejemplo, cuatro San Pedro en Santiago del Estero). No se filtran: el nombre para mostrar agrega el departamento solo en esos casos («San Pedro (Choya)»).
- **Actualización:** una copia nueva no se aplica sola sobre una base ya cargada; actualizar el catálogo queda pendiente hasta tener migraciones.

### Decisión: perfil de persona u organización (Dolores, 2026-11-05)

- **Un perfil por usuario**, de una **persona** o de una **organización** (refugio, grupo de rescate u ONG). Los dos tipos pueden adoptar y publicar: no hay limitaciones por tipo, solo cambian los datos que se piden. 
- **Persona** (todo obligatorio): nombre, apellido, género (Femenino, Masculino o X), descripción y localidad.
- **Organización:** nombre de la organización, descripción y localidad obligatorios; Instagram, Facebook y sitio web opcionales. Las redes son texto libre (usuario, link o nombre de página); el sitio web debe empezar con `http://` o `https://`.
- **Para más adelante**, porque dependen de decisiones abiertas: teléfono/WhatsApp y email de contacto (según cómo se resuelva el contacto), dirección física del refugio, CUIT o personería (solo si se verifican organizaciones) y persona responsable.
- **Modelo:** herencia JPA `JOINED` (`PersonProfile`, `OrganizationProfile`), porque se espera que los datos de cada tipo crezcan bastante.
- **Cuándo se pide:** inmediatamente después del login, si el usuario no tiene perfil. Se puede postergar («Completar más tarde»), pero al intentar publicar, contactar o guardar una publicación se lo redirige al alta.

### Decisión: publicación de adopción (Dolores, 2026-11-07)

- **Acceso:** el detalle de una publicación es público, sin login (el catálogo también lo será; llega en el PR siguiente). Publicar, editar, cambiar el estado y dar de baja requieren login. Contactar y guardar publicaciones, en el módulo siguiente, también.
- **Perfil obligatorio para publicar:** el backend rechaza el alta sin perfil (403 `PROFILE_REQUIRED`), además de la redirección del front. La misma regla valdrá para contactar y guardar.
- **Ficha** (todo obligatorio salvo el tamaño): nombre de la mascota (no un «título»), descripción, especie (perro o gato), sexo (macho, hembra o indeterminado), fecha de nacimiento estimada (mes y año), vacunado, desparasitado, castrado y localidad. La localidad se precarga con la del perfil pero se puede cambiar.
- **Tamaño:** solo para perros. Obligatorio para perros; no se carga para gatos.
- **Salud:** vacunado, desparasitado y castrado son **sí o no**. No existe la opción «no sé»: si no se sabe, se carga «no», por seguridad.
- **Edad:** se guarda la fecha de nacimiento estimada; la edad y la etapa de vida se calculan. Etapas: cachorro (menos de 1 año), adulto (de 1 a 7) y mayor (8 o más). La fecha no puede ser futura ni de hace más de 30 años.
- **Estados:** en adopción y adoptada. Toda publicación nace en adopción. Solo quien publicó cambia el estado, en los dos sentidos (para corregir un error o una adopción que no funcionó). Más adelante pueden sumarse pausada, reservada o borrador.
- **Catálogo y detalle:** el catálogo mostrará solo las publicaciones en adopción. El detalle de una adoptada sigue siendo público y muestra su estado, para que un link viejo compartido en redes diga «ya fue adoptada» y corte las consultas.
- **Edición:** se puede editar en cualquier estado. Quien publicó también puede editar desde el detalle: el detalle informa si quien mira es el dueño.
- **Baja:** lógica. La publicación desaparece del detalle y de «mis publicaciones», pero queda guardada.
- **Responsable visible:** nombre y apellido de la persona, o nombre de la organización, y el tipo de perfil. Nunca el email.
- **Para más adelante:** quién adoptó (depende del módulo de contacto), fotos (requiere acordar almacenamiento), raza, color, compatibilidad con chicos y otros animales, y el listado público por responsable.

`LOST` y `SERVICE` son otras categorías contempladas por el modelo de publicaciones, pero su alcance respecto del MVP debe definirse explícitamente.

## Estado observado del código (no requisitos aprobados)

| Pieza | Lo que existe | Lo que **no** se puede inferir |
| --- | --- | --- |
| Identidad | `User` guarda proveedor, ID externo, email y nombre; `POST /auth/oauth` busca o crea el usuario y emite JWT. | Google como única opción futura, cuentas verificadas, vinculación de cuentas o autorización para publicar. La validación de identidad actual tiene riesgos; ver [API](../arquitectura/api.md). |
| Perfil y ubicación | `Profile` (abstracta, herencia `JOINED` en `PersonProfile` y `OrganizationProfile`) se relaciona 1:1 con `User` (único por usuario) y N:1 con `Locality`; `Locality` pertenece a `Province`. `POST /profile` crea el perfil y `GET /profile` lo devuelve. Provincias y localidades se importan de Georef al arrancar y se consultan con `GET /provinces` y `GET /provinces/{id}/localities?q=`. El enum `Gender` (`FEMALE`, `MALE`, `X`) pertenece al **perfil de persona**, no a la mascota. | Edición de perfil, visibilidad pública de los datos del perfil ni verificación de organizaciones. |
| Publicación | Entidad abstracta `Publication` (herencia JPA `JOINED`) con tipo `ADOPTION`, `LOST` o `SERVICE`, descripción, autor obligatorio, localidad opcional y fechas de alta, modificación y baja lógica. Su único subtipo es `AdoptionPublication`. | Fotos ni publicaciones de perdidas o servicios. |
| Adopción | `AdoptionPublication` con la ficha y el estado. `POST /adoptions`, `GET /adoptions/{id}` (público, con `viewerIsOwner`), `PUT`, `PATCH /{id}/status`, `DELETE` (baja lógica) y `GET /adoptions/mine`, con permisos por dueño y perfil obligatorio para publicar. | Catálogo público con filtros (llega en el PR siguiente), solicitud o contacto, registro de quién adoptó ni fotos. |

Fuentes de este inventario: `src/main/java/com/adoptr/adoptr/{user,profile,location,publication,adoption}/model/`, `auth/service/AuthService.java`, `profile/controller/ProfileController.java`, `adoption/controller/AdoptionController.java` y [contrato HTTP observado](../arquitectura/api.md). Revisarlo al cambiar código: un enum, DTO o asociación JPA no aprueba una regla de negocio.

## Recorrido de producto a definir con el equipo

Como hipótesis para un MVP (no contrato aprobado): una persona responsable publica una mascota; visitantes la descubren y consultan; alguien interesado inicia una solicitud; la persona responsable responde y se registra un resultado. Antes de construirlo, cerrar estas preguntas:

1. ¿Requiere verificación publicar como organización? (Resuelto: publican personas y organizaciones con perfil, y el catálogo es público; ver la decisión de publicación de adopción.)
2. ¿Qué fotos son obligatorias? ¿Quién almacena las imágenes y por cuánto tiempo? (Resuelto: atributos de la ficha y datos visibles del responsable.)
3. ¿Cómo se evita recibir solicitudes de una publicación adoptada? (Resuelto: estados en adopción y adoptada, que cambia solo quien publicó.)
4. ¿Qué es una solicitud de adopción: formulario, contacto externo o conversación? ¿Quién la ve, cómo se notifica, se modera y se cierra?
5. ¿Qué ubicación mostrar y con qué precisión? ¿Qué políticas aplican a animales perdidos y servicios si se incorporan al alcance?

## Decisiones que habilitan implementación (pendientes)

- **Identidad y acceso:** acordar proveedores admitidos, uso de email verificado, duplicados/vinculación de cuentas y si la lectura del catálogo será anónima. La configuración actual exige JWT para toda ruta futura excepto `/auth/**`, Swagger y la lectura de provincias y localidades (pública por decisión de Dolores, 2026-11-07, para que visitantes anónimos puedan filtrar por ubicación): habilitar catálogo público requeriría cambiarla y probarlo; no basta con dibujar una pantalla pública.
- **Perfil y datos personales:** acordar cuándo se exige completar perfil, qué campos son necesarios, quién los puede consultar y si la localidad del perfil difiere de la de la publicación. Decidir fuente/actualización de provincias y localidades y precisión de la ubicación mostrada; no copiar datos de contacto al DTO público por comodidad.
- **Publicación y titularidad:** decidir datos de mascota, imágenes (almacenamiento, consentimiento y retención), quién puede crear/editar/cerrar y qué estados/transiciones existen. La relación `Publication.author` no verifica permisos por sí sola. Definir qué pasa con consultas pendientes al cerrar o adoptar.
- **Interés y resolución:** decidir si la interacción es formulario, contacto externo o mensajería; quién ve qué datos, notificaciones, moderación, consentimiento, retención y resultado. No crear estados ni endpoints de solicitud antes de acordarlo.
- **Otras categorías:** decidir explícitamente si `LOST` y `SERVICE` se implementan en el MVP o después; que aparezcan en `PublicationType` no significa que haya flujos disponibles.

Registrar cada acuerdo con **decisión, responsable/fecha, alcance, casos límite, datos visibles y criterio de aceptación**; hasta entonces mantenerlo como pregunta, no como contrato. Antes de declarar funcional un recorrido exigir endpoint y persistencia reales, permisos por dueño, validaciones, DTO público mínimo, errores y pruebas de éxito/rechazo/transición/privacidad, más UI integrada con estados. Hoy solo hay prueba de carga de contexto en backend. Referencias: [arquitectura](../arquitectura/lineamientos.md), [contrato API](../arquitectura/api.md) y [flujos frontend](../../../adoptr-front/docs/producto/flujos-producto.md).
