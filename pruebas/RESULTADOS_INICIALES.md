# Resultados iniciales de pruebas

## Ejecución del 10 de octubre de 2026

Se verificó la versión local de la aplicación de Transmetro con los cambios del directorio de trabajo, sobre el commit base `0ffd00f`. No se creó ningún commit ni se desplegó esta versión en Render.

El archivo Java utilizado fue `target/transmetro-operativo-0.0.1-SNAPSHOT.jar`, con SHA256 `4431ADE8EA6F6BBC6C2AAA02E00F715FDB6187127C051BE301FFEECACD1B0291`. Esta identificación corresponde a la aplicación compilada; los programas de prueba están en el directorio de trabajo.

El ambiente incluyó JDK 21.0.12.1, PostgreSQL local, la base exclusiva `transmetro_operativo_pruebas`, Node.js 24.6.0 y Cypress 15.21.1 con Electron 138 sin interfaz visible. Las pruebas no utilizaron la base de desarrollo ni el servicio público.

| Grupo | Ejecutadas | Aprobadas | Fallidas |
| --- | --- | --- | --- |
| Reglas de rutas en Java | 12 | 12 | 0 |
| Validación de solicitudes en Java | 2 | 2 | 0 |
| Acceso y navegación con Cypress | 5 | 5 | 0 |
| Catálogos con Cypress | 4 | 4 | 0 |
| Historial educativo con Cypress | 2 | 2 | 0 |
| Rutas con Cypress | 3 | 3 | 0 |
| Total | 28 | 28 | 0 |

Maven completó `package` correctamente. La ejecución final de Cypress terminó con código cero, sin casos omitidos, pendientes o reintentos automáticos dentro de la suite. Los casos manuales y futuros del plan no están incluidos en este total.

## Incidencia en la automatización

Las dos primeras ejecuciones Cypress aprobaron 12 de 14 casos. CAT-01 y CAT-03 fallaron porque la escritura automática comenzó antes de que el diálogo terminara de abrirse y tomara el foco. Al editar inmediatamente después de guardar también podía coincidir con el cierre y reinicio del formulario anterior.

Se corrigieron los programas de prueba para comprobar el foco del diálogo antes de escribir y su cierre completo antes de reabrirlo. No se cambiaron los datos esperados, no se agregaron pausas de duración fija y no se ocultaron fallos con reintentos. La tercera ejecución completa aprobó los 14 casos Cypress.

## Evidencias y límites

Los informes Java están en `target/surefire-reports/` y los informes XML Cypress en `test/resultados/`. Las capturas de los fallos iniciales están en `test/cypress/screenshots/`. Estas rutas reflejan la reorganización posterior de Cypress en `test/`. Son archivos locales excluidos de Git; los informes de distintas ejecuciones deben distinguirse por su fecha y no sumarse como si fueran una sola ejecución.

La verificación Java de rutas usa datos simulados. Los flujos Cypress de registro, modificación y consulta usan PostgreSQL real. Estas pruebas no certifican todas las reglas futuras, no constituyen una evaluación de carga y no equivalen a aceptación final del proyecto.

Quedan pendientes la revisión manual MAN-01 a MAN-04, la ampliación de catálogos CAT-05 y CAT-06, los controles pendientes de sesión y perfiles y las pruebas de funcionalidades aún no implementadas. No se configuró integración continua: la ejecución local debe iniciarse explícitamente después de cada cambio.

## Verificación después de reorganizar Cypress

El 10 de octubre se trasladaron las dependencias, configuración, auxiliares, pruebas y evidencias Cypress a `test/`. Se ejecutó de nuevo el conjunto completo mediante `npm.cmd --prefix test run test:e2e`: 14 casos aprobados, cero fallidos, cero omitidos y código de salida cero. La aplicación compilada y los scripts SQL permanecieron sin cambios durante esta reorganización.
