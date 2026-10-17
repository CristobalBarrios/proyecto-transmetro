# Plan de desarrollo

## Entrega actual

Preparar una demostración web con Java 21, Spring Boot y PostgreSQL. La demostración administra los catálogos base de Transmetro.

## Alcance

1. Administrar municipalidades, líneas, estaciones, parqueos, buses, pilotos, guardias y accesos.
2. Seleccionar las referencias directas requeridas para cada catálogo, como municipalidad, estación, línea o parqueo.
3. Mantener la estructura y los datos definidos en `10_Script_Base_de_Datos.sql` y `10a_Datos_Demostracion.sql` sin modificarlos.
4. Requerir un usuario y contraseña configurados como variables de entorno antes de consultar o registrar información.
5. Mostrar los registros en tablas y crear o modificar datos desde formularios modales.
6. Presentar una pantalla de inicio de sesión propia con opción para mostrar u ocultar la contraseña.
7. Configurar las estaciones de cada línea en orden, con distancias entre estaciones y distancia total; consultar sus accesos y la cantidad de buses asignados.
8. Registrar y modificar el historial educativo de cada piloto.
9. Mantener navegación lateral por módulos con una entidad por pantalla.

## Reglas implementadas en rutas

- Una estación puede pertenecer a varias líneas, pero no repetirse dentro de una misma ruta.
- El orden se genera consecutivamente al guardar toda la secuencia en una transacción.
- La primera estación tiene distancia cero; las siguientes requieren distancia positiva, con hasta tres decimales en kilómetros.
- Se puede reordenar o quitar estaciones desde la configuración. Al cambiar el orden deben revisarse las distancias de los nuevos tramos.
- No se cambia la ruta si hay recorridos en curso. Si la línea ya está operativa, se exige entre uno y dos buses por estación antes de aceptar la nueva configuración.
- Los catálogos pueden prepararse sin asignaciones completas. La pantalla muestra el cumplimiento de buses; la activación de líneas y la validación integral antes de operar pertenecen a la siguiente etapa.

## Verificación

Cada módulo se desarrollará junto con sus pruebas Java y Cypress. Antes de concluir un cambio se ejecutarán las pruebas del módulo, la regresión y la revisión manual del flujo. El alcance, los casos y los criterios de aceptación están en `pruebas/PLAN_PRUEBAS.md`; las instrucciones de ejecución están en `test/README.md`.

1. Cargar los scripts de base de datos en PostgreSQL.
2. Iniciar la aplicación con Java 21.
3. Confirmar que se muestran los datos de demostración de líneas y estaciones.
4. Registrar y modificar un registro de cada catálogo disponible.
5. Confirmar que una solicitud sin iniciar sesión se redirige al formulario de acceso personalizado.
6. Confirmar que las listas desplegables muestran las referencias registradas.
7. Configurar una ruta, reordenar estaciones y comprobar que se conservan el orden y las distancias después de recargar.
8. Verificar que se rechazan estaciones duplicadas y distancias no válidas.
9. Registrar varias formaciones para un piloto y modificar una de ellas; comprobar los límites del año de finalización (1900 a 2100).

## Siguientes módulos del enunciado

1. Validación de líneas operativas: todas sus estaciones asociadas, entre uno y dos buses por estación y cobertura de guardias por acceso. Impedir que los cambios posteriores rompan las reglas de una línea operativa.
2. Usuarios y perfiles del modelo aprobado, incluido el operador asociado a una estación y la identificación del responsable de las operaciones.
3. Asignación y finalización de guardias en accesos.
4. Recorridos: línea, bus, piloto y fechas; evitar buses o pilotos ocupados simultáneamente.
5. Visitas de estaciones en orden, ocupación y espera adicional de cinco minutos cuando la ocupación es inferior al 25%.
6. Alertas cuando los pasajeros esperando alcanzan el 150% de la capacidad del bus, y asignación del refuerzo.
7. Reportes de estaciones, líneas y buses asignados, conforme a las consultas de la entrega del 26 de septiembre.
8. Eventos operativos y bitácora previstos en el diseño aprobado.

## Despliegue de demostración

1. Compilar y ejecutar la aplicación con el `Dockerfile` incluido.
2. Configurar en Render el puerto asignado y las credenciales de PostgreSQL mediante variables de entorno.
3. Cargar el esquema y los datos de demostración antes de publicar el servicio.

## Fuera de alcance

- Mapas, GPS e integraciones externas.
- Arquitectura distribuida y monitoreo de producción.
