# Guía principal para trabajar en adoptr-back

Este repositorio es la API de Adoptr. Antes de modificarla, leer [arquitectura y prioridades técnicas](docs/arquitectura/lineamientos.md), [API](docs/arquitectura/api.md) y [dominio](docs/producto/dominio-y-alcance.md). Documentar decisiones de negocio con el equipo antes de implementar flujos.

## Reglas de trabajo

- Mantener la organización por dominio (`auth`, `user`, `profile`, `location`, `publication`) y `shared` para infraestructura transversal. Flujo preferido: controller → service → repository; exponer DTO, no entidades JPA.
- No declarar funcionalidades completas sin endpoint, persistencia, validaciones y pruebas. Verificar el contrato vigente antes de consumirlo desde el frontend.
- Mantener un identificador interno consistente entre JWT, filtro y servicios; no confiar en el `provider` enviado por el cliente. Consultar las prioridades técnicas en `docs/arquitectura/lineamientos.md`.
- No agregar secretos ni credenciales reales al repositorio; las credenciales presentes en `application.properties` son deuda de seguridad: externalizarlas y rotarlas antes de desplegar. Mantener configuración local de ejemplo sin valores sensibles.
- Cambios de base de datos: preferir migraciones versionadas, revisar restricciones/índices; no depender de `ddl-auto=update` fuera de desarrollo. Mantener autorización por dueño en operaciones sobre recursos.
- Nuevos contratos HTTP: documentar ruta, método, request/response, errores y autorización en `docs/arquitectura/api.md`; actualizar el modelo correspondiente del frontend cuando cambie el contrato.
- Cubrir casos exitosos, error y permisos con pruebas; no considerar un test de carga de contexto como cobertura funcional. Ejecutar `./gradlew test` (requiere JDK 25, PostgreSQL y configuración local según el caso) y reportar cuando no se pueda ejecutar.
- Separar en las PRs hechos del código, decisiones acordadas y propuestas. No trasladar resultados de una máquina personal a documentación común. Al cambiar arquitectura, contrato, prioridades o reglas de producto, **actualizar el documento existente** en `docs/arquitectura/` o `docs/producto/`; crear otro solo si hay un tema independiente y mantener los enlaces.

## Navegación

- Arquitectura y prioridades: [lineamientos](docs/arquitectura/lineamientos.md) · [contrato HTTP](docs/arquitectura/api.md)
- Producto: [dominio y decisiones abiertas](docs/producto/dominio-y-alcance.md)
- [Inicio rápido](README.md)
