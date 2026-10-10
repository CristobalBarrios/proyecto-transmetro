# Plan de desarrollo

## Entrega actual

Preparar una demostración web mínima con Java 21, Spring Boot y PostgreSQL. La demostración solo administra un tipo de entidad visible: las líneas de Transmetro.

## Alcance

1. Mostrar las líneas existentes en la tabla `linea`.
2. Agregar una línea mediante código y nombre.
3. Asociar cada nuevo registro a la municipalidad de demostración incluida en los scripts de la fase 4.
4. Mantener la estructura y los datos definidos en `10_Script_Base_de_Datos.sql` y `10a_Datos_Demostracion.sql` sin modificarlos.

## Verificación

1. Cargar los scripts de base de datos en PostgreSQL.
2. Iniciar la aplicación con Java 21.
3. Confirmar que se muestra la línea `DEMO-1`.
4. Registrar una nueva línea y comprobar que aparece en la lista.

## Despliegue de demostración

1. Compilar y ejecutar la aplicación con el `Dockerfile` incluido.
2. Configurar en Render el puerto asignado y las credenciales de PostgreSQL mediante variables de entorno.
3. Cargar el esquema y los datos de demostración antes de publicar el servicio.

## Fuera de alcance

- Estaciones, accesos, parqueos, buses, personal, seguridad, recorridos, alertas y eventos.
- Inicio de sesión, roles, bitácora, reportes, mapas, GPS e integraciones externas.
- Arquitectura distribuida y características de producción, como autenticación, roles y monitoreo.
