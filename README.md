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

Ejecute una sola vez los scripts originales, en este orden, sobre la base vacía:

```powershell
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -d transmetro_operativo -f "..\outputs\2026-09-26\fase_4_modelado\10_Script_Base_de_Datos.sql"
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -d transmetro_operativo -f "..\outputs\2026-09-26\fase_4_modelado\10a_Datos_Demostracion.sql"
```

## Ejecución

```powershell
.\mvnw.cmd spring-boot:run
```

Abra `http://localhost:8080`. La aplicación valida el esquema existente; no crea ni modifica tablas automáticamente.
