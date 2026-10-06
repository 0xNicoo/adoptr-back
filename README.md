# adoptr-back

API Java/Spring Boot para Adoptr. Ver [AGENT.md](AGENT.md), [arquitectura y prioridades técnicas](docs/arquitectura/lineamientos.md) y [contrato HTTP](docs/arquitectura/api.md) antes de agregar funcionalidades.

## Inicio rápido local (Windows/Linux)

Solo necesitás Docker con Compose (en Windows: Docker Desktop con contenedores Linux). Esto levanta **backend + PostgreSQL**; no incluye el frontend.

**1. Desde `adoptr-back`, copiá el archivo de ejemplo:**

```bash
# Linux
cp .env.local.example .env.local
```

```powershell
# Windows (PowerShell)
Copy-Item .env.local.example .env.local
```

Para este modo usamos **`.env.local.example`**, no el `.env.example` genérico. Si ya tenés `.env.local`, no lo sobrescribas.

**2. Abrí `.env.local` y completá solo estos dos valores:**

- `SPRING_DATASOURCE_PASSWORD`: elegí una contraseña para la base que Docker va a crear; no necesitás obtenerla de otro servicio.
- `SECURITY_JWT_SECRET`: generá un secreto y pegá el resultado completo. Linux: `openssl rand -hex 32`. PowerShell: `[guid]::NewGuid().ToString('N') + [guid]::NewGuid().ToString('N')`.

Dejá el resto como viene en el ejemplo. No subas `.env.local` a Git: está ignorado; el `.example` sí se puede subir.

**3. Levantá el backend y la base:**

```bash
docker compose --env-file .env.local up --build
```

Esperá a que termine de arrancar y abrí [Swagger](http://localhost:8081/swagger-ui/index.html). La primera vez puede tardar mientras descarga y compila.

**Para detener:** `Ctrl+C`. Los datos quedan guardados para la próxima ejecución. Esta base es independiente de tu PostgreSQL instalado; no importa sus datos. Si el puerto 8081 está ocupado, detené el backend anterior o cambiá `PORT` en `.env.local`. Cambiar la contraseña en el archivo después del primer arranque no modifica la contraseña de la base ya creada.

---

## Configuración

PostgreSQL y JWT requieren configuración externa, sin credenciales por defecto:

- `SPRING_DATASOURCE_URL`: URL JDBC de PostgreSQL.
- `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD`.
- `SECURITY_JWT_SECRET`: secreto nuevo de al menos 32 bytes (`openssl rand -hex 32`).
- Opcionales: `PORT` (8081), `SECURITY_JWT_EXPIRATION` (3600000 ms), `SPRING_JPA_HIBERNATE_DDL_AUTO` (`update`, solo desarrollo).

Los secretos anteriormente versionados deben rotarse; retirarlos del archivo no los elimina del historial. No reutilizar el JWT anterior. La dockerización no resuelve las limitaciones de autenticación descritas en [API](docs/arquitectura/api.md).

## Ejecutar sin Docker

Requisitos: JDK 25, PostgreSQL y conexión para resolver dependencias Gradle y claves públicas de Google. El wrapper usa Gradle 9.2.1.

```bash
export SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5432/adoptr'
export SPRING_DATASOURCE_USERNAME='postgres'
export SPRING_DATASOURCE_PASSWORD='<password-local>'
export SECURITY_JWT_SECRET='<secreto-nuevo-de-al-menos-32-bytes>'
./gradlew bootRun
```

## Docker Compose (recomendado para el equipo: Windows/Linux)

Requisitos: Docker con Compose v2; en Windows, Docker Desktop en modo contenedores Linux. No necesitan instalar Java ni PostgreSQL. Desde la raíz del backend, copiar el ejemplo una sola vez:

```bash
# Linux
cp .env.local.example .env.local
```

```powershell
# Windows PowerShell
Copy-Item .env.local.example .env.local
```

Completar `SPRING_DATASOURCE_PASSWORD` con una contraseña elegida para la nueva base Docker y `SECURITY_JWT_SECRET` con un secreto nuevo. Dejar `SPRING_DATASOURCE_USERNAME=postgres`. Para generar el secreto: `openssl rand -hex 32` en Linux; en PowerShell, `[guid]::NewGuid().ToString('N') + [guid]::NewGuid().ToString('N')`.

Arrancar (mismo comando en ambos sistemas):

```bash
docker compose --env-file .env.local up --build
```

Compose construye el backend usando el Dockerfile del repositorio, arranca PostgreSQL 17 y espera a que acepte conexiones antes de iniciar Spring Boot. La API queda en `http://localhost:8081` y Swagger en `/swagger-ui/index.html`. `PORT` cambia el puerto del host; dentro del contenedor se mantiene 8081.

**Es una base nueva e independiente de PostgreSQL instalado en la máquina.** No se copian datos automáticamente. Compose reemplaza `SPRING_DATASOURCE_URL` por `jdbc:postgresql://db:5432/adoptr`, sin modificar `.env.local`; no hace falta `host.docker.internal` ni red host. PostgreSQL no publica un puerto en el host, evitando conflicto con el servicio local. Los datos se conservan en el volumen `postgres_data`.

```bash
# Detener y retirar contenedores conservando los datos:
docker compose --env-file .env.local down
# Arrancar en segundo plano:
docker compose --env-file .env.local up --build -d
# Ver logs del backend:
docker compose --env-file .env.local logs -f backend
```

`Ctrl+C` detiene los servicios cuando corren en primer plano. No usar `down -v` salvo que quieran **borrar los datos de la base Docker**. El usuario/contraseña de PostgreSQL se inicializan solo con un volumen vacío: cambiarlos en `.env.local` no cambia las credenciales de una base ya creada. Los ejemplos usan sintaxis de Docker env: valores sin comillas; si una contraseña contiene `$`, usar comillas simples en `.env.local` para evitar interpolación de Compose.

## Docker sin Compose (opcional: PostgreSQL instalado en el host)

Desde la raíz de este repositorio:

```bash
cp .env.local.example .env.local
# Completar usuario, contraseña local y secreto JWT.
./run.sh                   # Equivale a ./run.sh local
```

Si ya tenían un `.env` funcionando con PostgreSQL local, pueden copiarlo a `.env.local` en lugar del ejemplo, verificando que la URL use `localhost`.

Para usar una base remota, si cuentan con sus datos de conexión:

```bash
cp .env.develop.example .env.develop
# Completar datos de conexión y secreto JWT.
./run.sh develop
```

El script construye la imagen en cada ejecución (Docker reutiliza su caché), selecciona el archivo según el entorno y arranca en primer plano. `Ctrl+C` detiene el backend y elimina el contenedor. Lee `PORT` del archivo (8081 por defecto). No arranca PostgreSQL, no realiza despliegues remotos y no ejecuta el contenido del archivo como código shell. Si ya hay otro proceso usando ese puerto, detenerlo o elegir otro `PORT`.

`.env.local` y `.env.develop` están ignorados por Git; sus archivos `.example` sí se versionan. No completar los ejemplos con secretos reales.

El Dockerfile compila el `bootJar` con JDK 25 y ejecuta solo el JAR con JRE 25 y un usuario sin privilegios. No necesita Java/Gradle instalados en el host. `.dockerignore` limita el contexto a los archivos de compilación; `.env` no se incorpora a la imagen.

El modo `local` requiere Linux y usa `--network host`: `localhost` dentro del contenedor apunta al host, permitiendo usar PostgreSQL que escucha en `127.0.0.1` sin abrirlo a otras interfaces. El modo `develop` usa la red bridge de Docker y publica el puerto HTTP solo en `127.0.0.1`; su base debe ser remota. En otros sistemas hay que adaptar la conexión al host. `bootRun` no carga estos archivos automáticamente. El `.env.example` anterior se conserva como referencia para el modo bridge con `host.docker.internal`, que requiere configurar el acceso de PostgreSQL desde Docker.

Swagger UI: `http://localhost:8081/swagger-ui/index.html`. No hay un endpoint de health dedicado por ahora.

## Pruebas

```bash
./gradlew test
```

Las pruebas que levantan contexto requieren una base de datos accesible y las variables obligatorias. La construcción Docker ejecuta `bootJar`, no las pruebas: ejecutarlas por separado antes del merge o en CI.

- Arquitectura y prioridades: [lineamientos](docs/arquitectura/lineamientos.md) · [contrato HTTP](docs/arquitectura/api.md)
- Producto: [dominio y decisiones abiertas](docs/producto/dominio-y-alcance.md)
