# Plan de Pruebas

Sistema de control operativo para Transmetro

Dirección de Movilidad Urbana

Municipalidad de Guatemala

Presento el plan de verificación de la aplicación de control operativo para Transmetro. La estrategia combina revisión manual, automatización de los flujos de usuario con Cypress y pruebas Java de las reglas de negocio. Su propósito es detectar defectos durante el desarrollo y aportar evidencia verificable para la evaluación y aceptación del sistema.

## Objetivo y alcance

Se verificará el cumplimiento del enunciado y del diseño aprobado: municipalidades, líneas, estaciones, accesos, parqueos, buses, pilotos, formación educativa, asignaciones de seguridad, recorridos, visitas, ocupación, alertas y reportes. Los usuarios, perfiles, eventos y bitácora previstos en el diseño también formarán parte de la verificación al implementarse.

La versión disponible administra catálogos, rutas de líneas e historial educativo y dispone de acceso protegido por usuario y contraseña. Los casos de funcionalidades pendientes se definen desde ahora; se ejecutarán cuando exista una versión verificable. La existencia de un caso o de un programa de prueba no constituye un resultado aprobado.

Las pruebas no evaluarán mapas, GPS ni integraciones externas. Tampoco se realizarán ataques de carga sobre el servicio público de demostración. La evaluación básica de seguridad cubre acceso, protección CSRF, validación de entradas y conservación de datos.

## Referencias de verificación

- Enunciado del proyecto Transmetro: reglas de líneas, estaciones, buses, pilotos, seguridad, ocupación y reportes.
- Análisis de requerimientos del 5 de septiembre: casos de uso CU-0001 a CU-0008.
- Modelo y scripts de PostgreSQL del 26 de septiembre: estructuras, relaciones y restricciones.
- Calendario de entregas: Plan de Pruebas y Carta de Aceptación el 17 de octubre de 2026; entrega final el 24 de octubre de 2026.

Cuando exista una diferencia entre la implementación y el enunciado, se registrará como incidencia o decisión pendiente; no se ajustará el resultado esperado para ocultarla. Los porcentajes de ocupación se probarán con los umbrales explícitos del enunciado.

## Estrategia y responsabilidades

La automatización Cypress verificará flujos completos desde el navegador, solicitudes a las API y persistencia sobre PostgreSQL real. Las pruebas Java verificarán reglas y límites de forma rápida y aislada. Las comprobaciones Java con datos simulados no sustituyen la verificación de transacciones contra PostgreSQL.

La revisión manual verificará legibilidad, colores, navegación, teclado, mensajes, uso en pantalla pequeña y coherencia de los reportes. Se revisarán además casos nuevos que no estén cubiertos por los programas automatizados.

Cristóbal Barrios Larios, como consultor externo, preparará los casos, mantendrá la automatización y registrará resultados e incidencias. La persona designada por la Dirección de Movilidad Urbana revisará los criterios funcionales y la aceptación. La firma de aceptación corresponderá al responsable autorizado y no se presumirá por el resultado de las pruebas técnicas.

Cada requisito deberá vincularse con al menos un caso. Los casos identificarán precondiciones, datos, pasos, resultado esperado, prioridad, método y estado de ejecución. Las evidencias identificarán fecha, versión de la aplicación, ambiente y persona o proceso ejecutor.

## Ambiente y datos

Las pruebas automáticas se ejecutarán localmente con Java 21, Spring Boot, PostgreSQL, Node.js y Cypress. La aplicación de pruebas utilizará un puerto distinto al de desarrollo y una base exclusiva llamada transmetro_operativo_pruebas. Se conservarán sin cambios los scripts aprobados; el esquema de pruebas se creará a partir del script original.

Antes de cada escenario Cypress se restaurarán los datos ficticios mínimos: una municipalidad, una línea en borrador, dos estaciones, un acceso, un parqueo y un piloto. Las credenciales se suministrarán por variables de entorno y no se incluirán en evidencias. Cada escenario será independiente de los resultados del anterior.

Los registros creados por la automatización no se cargarán en la base de la demostración pública. Los casos de límites incluirán valores válidos, inválidos y de frontera. Las futuras pruebas de concurrencia deberán usar PostgreSQL real y comprobar que dos solicitudes incompatibles no sean aceptadas simultáneamente.

## Ciclo de pruebas por funcionalidad

1. Definir los criterios de aceptación y los casos antes de desarrollar la funcionalidad.
2. Implementar la funcionalidad y sus pruebas Java y Cypress según corresponda.
3. Ejecutar las pruebas específicas del módulo y corregir los defectos encontrados.
4. Ejecutar la regresión automatizada existente y la revisión manual del flujo modificado.
5. Registrar resultados, evidencias y defectos; repetir los casos afectados después de corregirlos.
6. Incorporar el módulo a la regresión permanente cuando cumpla los criterios de terminación.

En el trabajo local, el desarrollador ejecutará las pruebas antes de dar por concluida cada modificación. La ejecución automática ante cambios en el repositorio se incorporará mediante integración continua cuando se configure; el ejecutor local no se activa por sí mismo al editar archivos.

## Casos automatizados de la versión disponible

Los siguientes casos están definidos en la primera suite Cypress. Su estado de ejecución se documentará en el registro de resultados, separado de este plan.

| Caso | Precondición y ejecución | Resultado esperado | Prioridad |
| --- | --- | --- | --- |
| ACC-01 | Sin sesión, abrir la aplicación. | Redirección al inicio de sesión y ausencia de datos protegidos. | Alta |
| ACC-02 | Enviar usuario inexistente y contraseña incorrecta. | Mensaje de credenciales incorrectas y acceso rechazado. | Alta |
| ACC-03 | Alternar Mostrar y Ocultar en la contraseña. | Cambia su visibilidad sin modificar su valor. | Media |
| NAV-01 | Iniciar sesión, abrir varias entidades y recargar. | Una sola pantalla visible y conservación de la ruta seleccionada. | Alta |
| SEG-01 | Con sesión, escribir en la API sin token CSRF. | Respuesta 403 y ausencia de escritura. | Alta |
| CAT-01 | Crear PRUEBA-2, modificar su nombre y recargar. | La línea conserva los datos modificados. | Alta |
| CAT-02 | Crear otra línea con el código PRUEBA-1. | Rechazo por duplicidad y cantidad de líneas sin cambios. | Alta |
| CAT-03 | Crear estación C con dirección, zona 3 y código PRUEBA-C. | Tabla con todos los valores registrados. | Alta |
| CAT-04 | Crear, modificar y consultar municipalidad, parqueo, bus, piloto, guardia y acceso mediante API. | Respuestas correctas y registros consultables. | Alta |
| RUT-01 | Guardar estaciones A y B con distancias 0 y 1.250 km. | Orden 1 y 2, distancia total 1.250 km y accesos asociados. | Alta |
| RUT-02 | Subir B al primer lugar, actualizar el tramo a 2.500 km y recargar. | Orden B y A persistente y distancia total correcta. | Alta |
| RUT-03 | Intentar repetir una estación en una ruta ya guardada. | Rechazo y conservación de la configuración anterior. | Alta |
| EDU-01 | Registrar formación del piloto, agregar título y recargar. | Formación vinculada al piloto y cambios persistentes. | Alta |
| EDU-02 | Usar años 1899, 1900, 2100, 2101 y año vacío; omitir nivel. | Acepta 1900, 2100 y año vacío; rechaza los límites inválidos y nivel vacío. | Alta |

Las pruebas Java complementan estos flujos con orden consecutivo, estación inexistente, distancia inicial distinta de cero, distancia posterior cero o negativa, recorrido en curso, línea operativa vacía, buses por debajo o por encima del límite y validación de precisión de distancias. Para una estación, se aceptan uno y dos buses y se rechazan cero y tres.

## Verificación manual y ampliación de catálogos

| Caso | Verificación | Resultado esperado | Método |
| --- | --- | --- | --- |
| MAN-01 | Revisar menú, iconos, encabezados y columnas en escritorio. | Grupos azules, opciones internas negras y contenido legible. | Manual |
| MAN-02 | Desplazar una tabla extensa y abrir formularios. | Barras visibles, formularios utilizables y mensajes sin superposición. | Manual |
| MAN-03 | Usar una pantalla pequeña y navegar con teclado. | Menú accesible y controles utilizables sin perder acciones. | Manual |
| MAN-04 | Explorar campos vacíos, cancelación, cambios de módulo y errores. | Mensajes comprensibles y ausencia de cambios involuntarios. | Manual |
| CAT-05 | Crear y modificar cada catálogo mediante su formulario. | Todos los campos conservados y referencias correctas. | Cypress por ampliar |
| CAT-06 | Probar longitudes máximas, zona cero, capacidad cero y referencias inexistentes. | Rechazo en la API y conservación de datos previos. | Java y Cypress por ampliar |
| SEG-02 | Cerrar sesión, repetir consulta y comprobar permisos por perfil. | Sesión cerrada y acceso limitado al perfil autorizado. | Pendiente de implementación y automatización |

## Casos de funcionalidades pendientes

Estos casos deben añadirse a la automatización durante el desarrollo del módulo indicado. No deben presentarse como aprobados hasta que se ejecuten sobre la aplicación.

| Caso | Regla o escenario | Resultado esperado | Prioridad |
| --- | --- | --- | --- |
| OPE-01 | Activar una línea de dos estaciones con 1, 2, 4 y 5 buses. | Acepta 2 y 4; rechaza 1 y 5. | Alta |
| OPE-02 | Cambiar buses o estaciones de una línea operativa. | No permite romper la relación de uno a dos buses por estación. | Alta |
| OPE-03 | Finalizar preparación con estación sin línea o acceso sin guardia. | Rechaza la habilitación operativa hasta completar asignaciones. | Alta |
| FLO-01 | Cambiar parqueo de un bus y dejarlo sin parqueo. | Permite cambio válido y rechaza parqueo vacío. | Alta |
| FLO-02 | Asignar el mismo bus a dos líneas. | Un bus mantiene como máximo una línea. | Alta |
| SEG-03 | Asignar y finalizar guardias en accesos. | Fechas coherentes y cobertura identificable por acceso. | Alta |
| USR-01 | Crear operador sin estación y probar otros perfiles. | Operador requiere estación; permisos corresponden al perfil. | Alta |
| REC-01 | Iniciar recorridos con bus y piloto disponibles. | Recorrido vinculado a la línea y a sus recursos. | Alta |
| REC-02 | Iniciar dos recorridos simultáneos con el mismo bus o piloto. | Solo una asignación incompatible puede aceptarse. | Alta |
| VIS-01 | Registrar visitas fuera del orden de la ruta. | Rechazo del orden incorrecto. | Alta |
| VIS-02 | Bus de capacidad 100 con 24 y 25 pasajeros a bordo. | Con 24 exige cinco minutos extra; con 25 no exige espera extra. | Alta |
| VIS-03 | Registrar 101 pasajeros o salida antes del tiempo permitido. | Rechazo por capacidad o salida prematura. | Alta |
| ALE-01 | Bus de capacidad 100 con 149 y 150 pasajeros esperando. | Con 149 no genera alerta; con 150 sí. | Alta |
| ALE-02 | Atender una alerta con un bus de refuerzo. | Refuerzo válido y alerta atendida con fecha. | Alta |
| REP-01 | Consultar estaciones, líneas y buses asignados. | Reportes coincidentes con las relaciones de PostgreSQL. | Alta |
| REP-02 | Consultar distancia total y accesos de una línea compartida. | Suma de tramos correcta y accesos de sus estaciones. | Alta |
| EVT-01 | Registrar, atender y cerrar un evento. | Estado, fechas y responsable coherentes. | Media |
| BIT-01 | Modificar datos sujetos a trazabilidad. | Bitácora con acción, fecha, usuario y registro afectados. | Media |

## Criterios de entrada y terminación

Una prueba podrá comenzar cuando exista una versión compilable, el requisito esté identificado, los datos necesarios estén disponibles y el ambiente de pruebas esté separado de la demostración. Un caso sin funcionalidad disponible se marcará Pendiente de implementación; si un problema del ambiente impide probarlo, se marcará Bloqueado.

Una funcionalidad se considerará terminada cuando pasen sus casos obligatorios, la regresión disponible permanezca aprobada, la revisión manual no identifique impedimentos de uso y no queden defectos críticos o altos abiertos en su alcance. Un defecto corregido requerirá repetir el caso y los escenarios afectados.

Para la aceptación final deberán estar implementados todos los requisitos obligatorios del enunciado, con sus casos de prioridad alta aprobados y evidencias disponibles. Los defectos menores y cualquier excepción deberán documentarse y ser evaluados por el responsable autorizado. La aceptación técnica parcial no implica aceptación final del proyecto.

## Registro de resultados y defectos

Cada ejecución registrará fecha, versión, ambiente, casos ejecutados y cantidades de aprobados, fallidos, bloqueados y no ejecutados. Los informes Java y Cypress se conservarán junto con la evidencia de errores y la revisión manual. No se sustituirá un fallo por un reintento aprobado sin investigar su causa.

Cada defecto tendrá identificador, caso relacionado, pasos reproducibles, resultado esperado, resultado obtenido, gravedad, estado y evidencia. Será crítico si impide acceso general o provoca pérdida de datos; alto si incumple una regla obligatoria o bloquea una función; medio si afecta un uso con alternativa; bajo si es visual y no impide operar.

Los resultados se actualizarán durante el desarrollo. Este plan se revisará para la entrega del 17 de octubre y para la verificación final del 24 de octubre de 2026.

Cristóbal Barrios Larios

Consultor externo
