# Ejecución de pruebas

El plan está en `../pruebas/PLAN_PRUEBAS.md`. La revisión del documento para la entrega está en [Plan de Pruebas](../../outputs/2026-10-17/14_Plan_de_Pruebas_Simplificado.docx) e incluye únicamente planificación. La [Carta de Aceptación de proyecto](../../outputs/2026-10-17/15_Carta_de_Aceptacion_Municipal_v1_0.docx), redactada como constancia municipal para la versión 1.0 y fecha 24 de octubre de 2026, queda pendiente de evaluación y firma municipal. Los resultados de una ejecución son evidencia de esa versión, no aprobación del proyecto completo.

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

Para repetir solo un archivo durante el diagnóstico, defina `E2E_SPEC` antes de ejecutar. Quite la variable al terminar para volver a ejecutar toda la batería:

```powershell
$env:E2E_SPEC = 'cypress/e2e/cu-0004-asignacion-operativa.cy.js'
npm.cmd --prefix test run test:e2e
Remove-Item Env:E2E_SPEC
```

La configuración, dependencias, programas de prueba y auxiliares de Cypress están en `test/`. Las pruebas Java permanecen en `src/test/java`, conforme a la estructura Maven.

Los resultados XML están en `test/resultados/` y `target/surefire-reports/`; las capturas de fallos están en `test/cypress/screenshots/`. Esos archivos no se versionan. Conserve los informes de cada evaluación formal junto con su identificador de versión. `test/package-lock.json` sí se versiona para reproducir las dependencias.

## Al desarrollar un módulo

1. Agregue sus casos y criterios al plan antes de desarrollar.
2. Incorpore las pruebas Java de reglas y Cypress de los flujos y persistencia.
3. Ejecute Maven `package`, la suite Cypress y la revisión manual del cambio.
4. Registre resultados y defectos. No termine el módulo con casos obligatorios fallidos.

La ejecución es local y explícita. La integración continua queda pendiente de configuración; ninguna de estas órdenes crea commits ni despliega la aplicación.

## Alcance simplificado

Se conserva una prueba del flujo principal por funcionalidad. Los catálogos se prueban por separado, uno por entidad. Los recorridos se inician directamente con recursos preparados, sin activar una línea previamente. Las pruebas de eventos cubren su registro y consulta, sin un ciclo de atención o cierre. Las validaciones permanecen en la aplicación, pero no se certifican exhaustivamente con este conjunto reducido.

## Organización por caso de uso

El identificador `CP-NNNN-XX` relaciona cada prueba con `CU-NNNN`; `XX` es el consecutivo dentro de ese caso de uso. El mismo identificador se utiliza en el plan y en el título de Cypress, seguido del nombre de la funcionalidad. No se reutilizan identificadores retirados. Las revisiones manuales transversales usan `CP-TR-XX`.

| Archivo en `cypress/e2e/` | Caso de uso | Pruebas automatizadas |
| --- | --- | --- |
| `cu-0001-lineas-estaciones.cy.js` | CU-0001 Administrar líneas y estaciones | CP-0001-01 a CP-0001-03 |
| `cu-0002-flota-parqueos.cy.js` | CU-0002 Administrar flota y parqueos | CP-0002-01 a CP-0002-02 |
| `cu-0003-personal-seguridad.cy.js` | CU-0003 Administrar personal operativo y seguridad | CP-0003-01 a CP-0003-03 |
| `cu-0004-asignacion-operativa.cy.js` | CU-0004 Registrar asignación operativa | CP-0004-04 y CP-0004-05 |
| `cu-0005-ocupacion-alertas.cy.js` | CU-0005 Registrar ocupación y gestionar alertas | CP-0005-01 a CP-0005-04 |
| `cu-0006-reportes.cy.js` | CU-0006 Consultar reportes operativos | CP-0006-01 a CP-0006-03 |
| `cu-0007-accesos-trazabilidad.cy.js` | CU-0007 Administrar accesos y trazabilidad | CP-0007-01 a CP-0007-06 |
| `cu-0008-datos-referencia.cy.js` | CU-0008 Actualizar datos de referencia | CP-0008-01 a CP-0008-02 |

Solo se mantienen pruebas ejecutables contempladas en el plan. No se crean archivos vacíos ni pruebas omitidas para los casos de uso pendientes de automatización. El inicio de sesión y la preparación de datos compartidos son auxiliares, no pruebas adicionales. Los informes históricos conservan los identificadores que se usaron en sus ejecuciones; no se reescriben como si fueran resultados de esta organización.
