# Plan de desarrollo

## Entrega actual

Completar la aplicación web con Java 21, Spring Boot y PostgreSQL según el enunciado y los ocho casos de uso entregados, conservando el esquema SQL existente.

## Alcance

1. Administrar municipalidades, líneas, estaciones, parqueos, buses, pilotos, guardias y accesos.
2. Seleccionar las referencias directas requeridas para cada catálogo, como municipalidad, estación, línea o parqueo.
3. Mantener la estructura y los datos definidos en `10_Script_Base_de_Datos.sql` y `10a_Datos_Demostracion.sql` sin modificarlos.
4. Autenticar usuarios registrados en PostgreSQL, con perfiles y operador por estación. Conservar las variables de entorno para la cuenta administradora inicial.
5. Mostrar los registros en tablas y crear o modificar datos desde formularios modales.
6. Presentar una pantalla de inicio de sesión propia con opción para mostrar u ocultar la contraseña.
7. Configurar las estaciones de cada línea en orden, con distancias entre estaciones y distancia total; consultar sus accesos y la cantidad de buses asignados.
8. Registrar y modificar el historial educativo de cada piloto.
9. Mantener navegación lateral por módulos con una entidad por pantalla.
10. Asignar y finalizar guardias en accesos, con fechas e identificación del responsable autenticado.
11. Activar y desactivar líneas, mostrando sus pendientes de preparación.
12. Conservar las condiciones de las líneas operativas al modificar rutas, buses, accesos o asignaciones de guardias.
13. Iniciar y finalizar recorridos con bus y piloto disponibles; registrar sus visitas en el orden de la ruta.
14. Calcular la espera adicional de cinco minutos con ocupación menor al 25% y las alertas desde el 150% de pasajeros esperando, usando las columnas calculadas del esquema original.
15. Registrar salidas y asignar otro bus disponible como refuerzo de una alerta.
16. Registrar, atender y cerrar eventos operativos con fechas y responsables.
17. Consultar reportes de estaciones, líneas, buses, rutas, accesos, pilotos, recorridos, visitas, alertas y eventos. Los reportes operativos permiten filtrar por período y recurso.
18. Registrar los cambios en bitácora con usuario, fecha, entidad y registro.
19. Cargar rutas y estaciones desde un archivo JSON de fuente autorizada, mostrando las rutas rechazadas sin guardarlas parcialmente.

## Reglas implementadas en rutas

- Una estación puede pertenecer a varias líneas, pero no repetirse dentro de una misma ruta.
- El orden se genera consecutivamente al guardar toda la secuencia en una transacción.
- La primera estación tiene distancia cero; las siguientes requieren distancia positiva, con hasta tres decimales en kilómetros.
- Se puede reordenar o quitar estaciones desde la configuración. Al cambiar el orden deben revisarse las distancias de los nuevos tramos.
- No se cambia la ruta si hay recorridos en curso. Si la línea ya está operativa, se exige entre uno y dos buses por estación antes de aceptar la nueva configuración.
- Los catálogos pueden prepararse sin asignaciones completas mientras la línea no esté operativa. La pantalla Operación permite revisar los pendientes y activar una línea cuando su ruta tiene estaciones, cada estación tiene accesos y un operador activo, la cantidad de buses está entre uno y dos por estación y todos sus accesos tienen guardia activo.
- Los cambios de preparación se serializan en PostgreSQL y se confirman en una sola transacción. Si dejan una línea operativa sin esas condiciones, se rechazan y no se guardan parcialmente. No se puede desactivar una línea con recorridos en curso.
- Las asignaciones registran inicio y fin con la hora del servidor. Para sustituir un guardia de una línea operativa, se asigna primero el reemplazo. El responsable proviene de la cuenta autenticada. Las contraseñas se almacenan con BCrypt y los permisos se verifican en el servidor.

## Verificación

Cada módulo se desarrollará junto con sus pruebas Java y Cypress. Antes de concluir un cambio se ejecutarán las pruebas del módulo, la regresión y la revisión manual del flujo. El alcance, los casos y los criterios de aceptación están en `pruebas/PLAN_PRUEBAS.md`; las instrucciones de ejecución están en `test/README.md`.

La automatización conserva una sola prueba del flujo principal por funcionalidad, sin casos de borde, negativos ni pruebas parametrizadas de límites. Las validaciones de la aplicación no se eliminan; esta reducción de pruebas implica menor cobertura automatizada.

1. Cargar los scripts de base de datos en PostgreSQL.
2. Iniciar la aplicación con Java 21.
3. Confirmar que se muestran los datos de demostración de líneas y estaciones.
4. Registrar y modificar un registro de cada catálogo disponible.
5. Confirmar que una solicitud sin iniciar sesión se redirige al formulario de acceso personalizado.
6. Confirmar que las listas desplegables muestran las referencias registradas.
7. Configurar una ruta, reordenar estaciones y comprobar que se conservan el orden y las distancias después de recargar.
8. Verificar que se rechazan estaciones duplicadas y distancias no válidas.
9. Registrar varias formaciones para un piloto y modificar una de ellas; comprobar los límites del año de finalización (1900 a 2100).

## Flujo de uso

1. Preparar los catálogos, la ruta, los buses, los guardias y los operadores de cada estación.
2. Activar la línea e iniciar un recorrido con un bus y un piloto disponibles.
3. Registrar cada visita con llegada, salida prevista y pasajeros; el sistema calcula espera extra y alerta. Registrar la salida después de la espera exigida.
4. Atender las alertas con otro bus disponible y finalizar el recorrido tras completar sus visitas.
5. Consultar los reportes, eventos y bitácora. El operador registra visitas y consulta operación de su estación; el administrador gestiona usuarios.

La carga de referencias usa `pruebas/REFERENCIAS_EJEMPLO.json` como formato. Se confirman la fuente y su autorización antes de cargar. No se incorporan mapas, GPS, conexiones a portales externos, recuperación de contraseñas, notificaciones ni componentes distribuidos.

## Despliegue de demostración

1. Compilar y ejecutar la aplicación con el `Dockerfile` incluido.
2. Configurar en Render el puerto asignado y las credenciales de PostgreSQL mediante variables de entorno.
3. Cargar el esquema y los datos de demostración antes de publicar el servicio.

## Fuera de alcance

- Mapas, GPS e integraciones externas.
- Arquitectura distribuida y monitoreo de producción.
