# adoptr-back

API Java/Spring Boot de Adoptr. Ver [arquitectura](docs/arquitectura/lineamientos.md), [contrato HTTP](docs/arquitectura/api.md) y [dominio](docs/producto/dominio-y-alcance.md) antes de agregar funcionalidades.

## Levantar localmente (Windows/Linux)

Necesitás Docker con Compose; en Windows, Docker Desktop con contenedores Linux. No hace falta instalar Java ni PostgreSQL. Esto levanta el backend y su base, no el frontend.

**1. Creá `.env.local` dentro de `adoptr-back`** (si ya existe, no lo sobrescribas):

```dotenv
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/adoptr
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=<elegí-una-contraseña>
SECURITY_JWT_SECRET=<generá-un-secreto-nuevo>
SECURITY_JWT_EXPIRATION=3600000
SPRING_JPA_HIBERNATE_DDL_AUTO=update
PORT=8081
```

Reemplazá los valores entre `<...>`. La contraseña es para la base que Docker va a crear. Para generar el secreto JWT:

- Linux: `openssl rand -hex 32`
- PowerShell: `[guid]::NewGuid().ToString('N') + [guid]::NewGuid().ToString('N')`

Pegá el resultado completo en `SECURITY_JWT_SECRET`. Los archivos `.env`, `.env.local` y `.env.develop` están ignorados por Git: **no subirlos ni compartir secretos**. Para evitar diferencias entre Compose y Docker, preferí valores sin comillas ni `$`.

**2. Desde `adoptr-back`, ejecutá:**

```bash
docker compose --env-file .env.local up --build
```

La primera vez descarga y compila. Cuando termine de arrancar, abrí [Swagger](http://localhost:8081/swagger-ui/index.html).

**3. Para detener:** `Ctrl+C`. Para retirar los contenedores conservando los datos:

```bash
docker compose --env-file .env.local down
```

Los datos quedan guardados en un volumen. **No usar `down -v` salvo que quieras borrarlos.** Esta base es nueva e independiente de PostgreSQL instalado en tu máquina; no importa sus datos. Compose adapta la URL al servicio `db` y espera a que PostgreSQL acepte conexiones. Cambiar la contraseña en `.env.local` después del primer arranque no cambia la contraseña de la base existente.

Si el puerto 8081 está ocupado, detené el backend anterior o cambiá `PORT` en `.env.local`.

## Otras formas de ejecutar

- **PostgreSQL instalado en el host (Linux):** usá sus credenciales en `.env.local`, la URL con `localhost` y ejecutá `./run.sh local`. Solo levanta el backend, usando la red del host.
- **Base remota:** creá `.env.develop` con las mismas variables, pero con URL JDBC, usuario y contraseña de esa base; ejecutá `./run.sh develop`. Corre Docker localmente, no realiza despliegues remotos.
- **Sin Docker:** necesitás JDK 25 y PostgreSQL. Exportá las variables en tu terminal y ejecutá `./gradlew bootRun`. Gradle no carga los archivos `.env` automáticamente.

Los scripts construyen el mismo Dockerfile multietapa con JDK/JRE 25 y usuario sin privilegios. No incluyen secretos en la imagen. `ddl-auto=update` es para desarrollo; para producción se requieren migraciones y revisión de seguridad. Los secretos anteriormente versionados deben rotarse: borrarlos del archivo no los elimina del historial. Consultar las limitaciones de autenticación en el [contrato HTTP](docs/arquitectura/api.md).

## Pruebas

```bash
./gradlew test
```

Requieren PostgreSQL accesible y las variables de configuración exportadas. La construcción de la imagen ejecuta `bootJar`, no las pruebas.
