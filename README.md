# Sistema de control operativo para Transmetro

Demostración web mínima para registrar líneas de Transmetro. Usa sin modificaciones el modelo relacional aprobado el 26 de septiembre de 2026.

## Alcance de esta entrega

- Ver líneas registradas.
- Agregar una línea a la municipalidad de demostración creada por el script de datos.

## Requisitos

- JDK 21.
- PostgreSQL 18 o compatible.
- Una base de datos vacía llamada `transmetro_operativo` cargada con los scripts de la fase 4.

## Configuración local

La contraseña no se guarda en el proyecto. En PowerShell, defínala antes de iniciar la aplicación:

```powershell
$env:TRANSMETRO_DB_PASSWORD = "SU_CONTRASENA"
```

Si su usuario o base de datos es diferente, configure también `TRANSMETRO_DB_USER` y `TRANSMETRO_DB_URL`.

Defina además un usuario y contraseña de acceso. No tienen valores predeterminados para evitar publicar la aplicación sin protección:

```powershell
$env:APP_USERNAME = "operador"
$env:APP_PASSWORD = "UseUnaContrasenaLargaYUnica"
```

Ejecute una sola vez los scripts originales, en este orden, sobre la base vacía:

```powershell
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -d transmetro_operativo -f "..\outputs\2026-09-26\fase_4_modelado\10_Script_Base_de_Datos.sql"
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -d transmetro_operativo -f "..\outputs\2026-09-26\fase_4_modelado\10a_Datos_Demostracion.sql"
```

## Ejecución local con IntelliJ IDEA

1. Abra la carpeta `proyecto-transmetro` como proyecto Maven.
2. Configure las variables de entorno de la sección anterior en la configuración de ejecución.
3. Ejecute la clase `TransmetroOperativoApplication`.

Abra `http://localhost:8080`. La aplicación valida el esquema existente; no crea ni modifica tablas automáticamente.

## Despliegue de demostración en Render

El repositorio incluye un `Dockerfile` que compila la aplicación con Maven y la ejecuta con Java 21. Antes de crear el servicio web:

1. Publique el proyecto en GitHub o GitLab. No publique las contraseñas de PostgreSQL.
2. En Render, cree una base de datos PostgreSQL y ejecute los scripts `10_Script_Base_de_Datos.sql` y `10a_Datos_Demostracion.sql` sobre ella.
3. Cree un servicio web desde el repositorio, seleccione el entorno `Docker` y elija el plan disponible para demostraciones.
4. Configure estas variables de entorno en el servicio:

```text
TRANSMETRO_DB_URL=jdbc:postgresql://HOST:PUERTO/BASE_DE_DATOS
TRANSMETRO_DB_USER=USUARIO
TRANSMETRO_DB_PASSWORD=CONTRASENA
APP_USERNAME=USUARIO_DE_ACCESO
APP_PASSWORD=CONTRASENA_LARGA_Y_UNICA
```

Render define la variable `PORT`; la aplicación la usa automáticamente. Todos los recursos requieren iniciar sesión y los registros usan protección CSRF. La instancia limita las conexiones HTTP y de PostgreSQL para ajustarse a una demostración pequeña. Esto reduce el riesgo de abuso accidental o de registros no autorizados, pero no sustituye la protección de red contra ataques de denegación de servicio a gran escala.
