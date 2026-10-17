# Ejecución de pruebas

El plan está en `../pruebas/PLAN_PRUEBAS.md`. El documento para la entrega está en [Plan de Pruebas](../../outputs/2026-10-17/14_Plan_de_Pruebas.docx). Los resultados de una ejecución son evidencia de esa versión, no aprobación del proyecto completo.

La primera verificación está documentada en `../pruebas/RESULTADOS_INICIALES.md`.

## Java

En IntelliJ, ejecute los tests de `src/test/java`, o la tarea Maven `test`. La tarea `package` ejecuta los tests y prepara el archivo que usa Cypress. Use JDK 21. Las pruebas Java actuales verifican reglas con datos simulados y restricciones de solicitudes; no usan la base de desarrollo.

## Cypress local

Desde la carpeta del proyecto:

```powershell
npm.cmd --prefix test ci
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21.0.12.1'
$env:E2E_DB_PASSWORD = $env:TRANSMETRO_DB_PASSWORD
# Opcionales si PostgreSQL no usa los valores predeterminados:
$env:E2E_DB_USER = 'postgres'
$env:E2E_DB_PORT = '5432'
# Compile con la tarea Maven package de IntelliJ antes de ejecutar:
npm.cmd --prefix test run test:e2e
```

El usuario de PostgreSQL debe tener permiso para crear la base local `transmetro_operativo_pruebas`, o esta debe existir previamente y permitir crear las tablas. El ejecutor solo acepta nombres terminados en `_pruebas` y conexiones a PostgreSQL local. El servidor de pruebas escucha en `127.0.0.1:8081`, inicia con una contraseña aleatoria y se detiene después de las pruebas. No cambie la URL de Cypress para apuntar a Render.

La base se crea, si falta, con el esquema SQL de la entrega anterior. Antes de cada escenario se vacían únicamente sus tablas de prueba y se restauran los datos mínimos. No ejecute dos suites al mismo tiempo sobre la misma base. Si el puerto 8081 está ocupado, use `E2E_APP_PORT` con otro puerto disponible.

Para ver la ejecución de forma interactiva:

```powershell
npm.cmd --prefix test run test:e2e:open
```

La configuración, dependencias, programas de prueba y auxiliares de Cypress están en `test/`. Las pruebas Java permanecen en `src/test/java`, conforme a la estructura Maven.

Los resultados XML están en `test/resultados/` y `target/surefire-reports/`; las capturas de fallos están en `test/cypress/screenshots/`. Esos archivos no se versionan. Conserve los informes de cada evaluación formal junto con su identificador de versión. `test/package-lock.json` sí se versiona para reproducir las dependencias.

## Al desarrollar un módulo

1. Agregue sus casos y criterios al plan antes de desarrollar.
2. Incorpore las pruebas Java de reglas y Cypress de los flujos y persistencia.
3. Ejecute Maven `package`, la suite Cypress y la revisión manual del cambio.
4. Registre resultados y defectos. No termine el módulo con casos obligatorios fallidos.

La ejecución es local y explícita. La integración continua queda pendiente de configuración; ninguna de estas órdenes crea commits ni despliega la aplicación.
