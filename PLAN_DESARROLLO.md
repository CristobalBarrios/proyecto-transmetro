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

## Verificación

1. Cargar los scripts de base de datos en PostgreSQL.
2. Iniciar la aplicación con Java 21.
3. Confirmar que se muestran los datos de demostración de líneas y estaciones.
4. Registrar y modificar un registro de cada catálogo disponible.
5. Confirmar que una solicitud sin iniciar sesión se redirige al formulario de acceso personalizado.
6. Confirmar que las listas desplegables muestran las referencias registradas.

## Despliegue de demostración

1. Compilar y ejecutar la aplicación con el `Dockerfile` incluido.
2. Configurar en Render el puerto asignado y las credenciales de PostgreSQL mediante variables de entorno.
3. Cargar el esquema y los datos de demostración antes de publicar el servicio.

## Fuera de alcance

- Educación de pilotos, asignaciones de guardias, relación línea-estación, recorridos, visitas, alertas, eventos y bitácora.
- Usuarios internos, roles, reportes, mapas, GPS e integraciones externas.
- Arquitectura distribuida y características de producción, como autenticación, roles y monitoreo.
