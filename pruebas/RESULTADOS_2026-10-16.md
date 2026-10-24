# Verificación del 16 de octubre de 2026

## Versión y ambiente

Se verificaron los cambios locales sobre el commit base `37ef9c9`, sin crear commits ni desplegar en Render. El código fuente y los programas de prueba de la copia de verificación se compararon por su contenido con los del proyecto.

Una sesión anterior de Cypress mantenía abierta la aplicación en el puerto 8081 y bloqueaba el empaquetado de su JAR. Para no interrumpirla, la compilación final y la ejecución se hicieron en `.tmp/transmetro-verificacion`, fuera del repositorio de la aplicación, con el puerto 8082 y la base PostgreSQL independiente `transmetro_revision_pruebas`. No se vació la base utilizada por la sesión anterior.

Se utilizaron JDK 21.0.12.1, Maven de IntelliJ, PostgreSQL local y Cypress 15.21.1 con Electron sin interfaz visible. El esquema provino de una copia sin cambios del script aprobado del 26 de septiembre.

## Resultados

| Conjunto | Ejecutadas | Aprobadas | Fallidas |
| --- | --- | --- | --- |
| Java | 7 | 7 | 0 |
| Cypress | 16 | 16 | 0 |

Maven completó `package` correctamente en la copia independiente. Cypress finalizó con código cero, sin casos omitidos ni reintentos automáticos.

Las pruebas Java cubrieron los flujos principales de guardar una ruta, registrar formación, asignar un guardia, finalizar una asignación, activar una línea, desactivarla y comprobar su preparación. Las pruebas Cypress cubrieron inicio de sesión, ocho catálogos por separado, ruta, formación, asignación y finalización de guardias y tres operaciones de preparación de líneas.

## Alcance reducido

Se conserva una sola prueba por funcionalidad, con datos válidos del flujo principal. Se eliminaron escenarios negativos, parametrizados de límites y de borde; no se reunieron dentro de una prueba grande. Las validaciones continúan en el código, pero no se considera certificada su cobertura negativa ni concurrente con esta ejecución.

La revisión visual completa de la aplicación y la aceptación municipal permanecen pendientes. El documento Word actualizado del plan sí se renderizó y revisó por páginas. El Word anterior no se sobrescribió porque estaba bloqueado para escritura.

Las evidencias de esta ejecución están en `test/resultados/2026-10-16/`, excluidas de Git. Para probar la versión actual en la carpeta habitual, cierre antes la instancia anterior de Cypress y su servidor y ejecute Maven `package`; después use los comandos de `test/README.md`. No se requieren cambios de esquema ni nuevas variables de entorno.
