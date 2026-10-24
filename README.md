# Sistema de control operativo para Transmetro

Aplicación web para controlar la red y la operación de Transmetro. Usa sin modificaciones el modelo relacional aprobado el 26 de septiembre de 2026.

## Alcance de esta entrega

- Ver, agregar y modificar municipalidades, líneas y estaciones.
- Ver, agregar y modificar parqueos y buses.
- Ver, agregar y modificar pilotos, guardias y accesos.
- Seleccionar las referencias directas necesarias desde listas desplegables.
- Iniciar sesión desde una pantalla propia que permite mostrar u ocultar la contraseña.
- Configurar el orden de estaciones de una línea, las distancias entre estaciones y consultar distancia total, accesos y buses asignados.
- Registrar y modificar la formación académica de cada piloto desde Historial educativo.
- Asignar y finalizar guardias desde Seguridad > Asignaciones, conservando fechas y responsable.
- Conservar la pertenencia de las estaciones a líneas, el rango de buses por línea, la cobertura de guardias y un operador activo por estación al guardar cambios, haya o no recorridos en curso.
- Administrar usuarios y perfiles; asociar cada operador a su estación.
- Iniciar y finalizar recorridos, registrar visitas ordenadas, ocupación y salidas con la espera adicional requerida.
- Generar alertas de ocupación y asignar buses de refuerzo.
- Registrar eventos para su consulta conforme a CU-0006 y consultar la bitácora de cambios de CU-0007.
- Consultar reportes de red, flota, personal y operación; filtrar los operativos por período y recurso.
- Cargar referencias de rutas y estaciones desde JSON autorizado y revisar las rutas rechazadas.

Para comenzar sin registros, cree la municipalidad, un parqueo sin estación y un bus con parqueo pero sin línea. En **Nueva línea**, registre también su primera estación, el bus inicial y los datos del operador. Se guardan juntos: no queda una línea vacía ni una estación sin línea u operador. El operador se crea con perfil fijo Operador y contraseña codificada; la administración general de cuentas sigue en Administración > Usuarios.

En **Nueva estación**, seleccione su línea, distancia desde la última estación y datos del operador. Si hace falta aumentar la cantidad de buses, seleccione uno sin línea en el mismo formulario. En **Nuevo acceso**, seleccione el primer guardia. Estas altas se confirman completas o se revierten.

En **Red > Rutas de líneas**, seleccione una línea y abra **Configurar ruta**. Agregue estaciones existentes, ordénelas con Subir/Bajar y registre la distancia desde la anterior. La primera tiene distancia cero. El guardado reemplaza la secuencia completa de forma transaccional; rechaza duplicados, dejar estaciones sin ninguna línea o cantidades de buses incompatibles. No admite cambios mientras haya recorridos en curso. Para retirar una estación de su última línea, asígnela primero a otra.

En **Personal > Historial educativo**, agregue las formaciones y seleccione el piloto correspondiente. El título y el año de finalización son opcionales.

Estas funciones utilizan las tablas existentes. No requieren volver a ejecutar los scripts ni cambiar el esquema. El alcance y flujo de uso están en `PLAN_DESARROLLO.md`.

Las líneas conservan entre uno y dos buses por estación; cada acceso existente tiene al menos un guardia y cada estación tiene un operador activo. Estas condiciones se validan aunque no haya recorridos en curso. Para sustituir un guardia u operador, registre primero el reemplazo. No es necesario activar líneas ni exigir un mínimo adicional de accesos por estación.

La autenticación utiliza la tabla `usuario` y contraseñas BCrypt. `APP_USERNAME` y `APP_PASSWORD` configuran la cuenta administradora inicial: al iniciar se crea o actualiza esa cuenta, incluido su perfil y contraseña. Las demás cuentas se gestionan en **Administración > Usuarios**. Los usuarios de los datos SQL de ejemplo contienen hashes de marcador; establezca una contraseña desde esta pantalla antes de usarlos. No se agregan variables de entorno ni se modifican los scripts SQL.

En **Operación > Recorridos**, asigne un bus de la línea y un piloto disponibles. En **Visitas**, registre la llegada, salida prevista y pasajeros en el orden de la ruta. Si la ocupación es menor al 25%, la salida debe respetar cinco minutos adicionales; con pasajeros esperando desde el 150% de capacidad se genera una alerta. En **Alertas**, asigne otro bus disponible como refuerzo. Finalice el recorrido después de registrar todas sus salidas.

Los perfiles del modelo limitan las modificaciones de red, flota, personal y seguridad. El operador está asociado a una estación y solo registra sus visitas; el administrador administra cuentas. La cuenta inicial configurada por variables de entorno se reserva para administración.

En **Red > Datos de referencia**, seleccione un archivo con el formato de `pruebas/REFERENCIAS_EJEMPLO.json`, identifique su fuente y confirme su autorización. El ejemplo actualiza la línea 1 de la muestra del script de demostración y utiliza distancias simuladas. Para incorporar una línea nueva, agregue `busIds` con suficientes buses existentes sin línea; cada estación nueva requiere un objeto `operador` con `nombre`, `usuario` y `contrasena`. No publique archivos que contengan contraseñas. Cada ruta se guarda completa o se rechaza con un motivo. No se consultan servicios externos.

## Pruebas durante el desarrollo

El proyecto incluye pruebas Java y Cypress sobre una base PostgreSQL local independiente. Cypress y sus dependencias están separados en `test/`. Consulte `test/README.md` para ejecutarlas y `pruebas/PLAN_PRUEBAS.md` para los casos y criterios de aceptación. Para las pruebas Cypress se requiere Node.js; la aplicación desplegada sigue ejecutándose únicamente con Java.

## Requisitos de ejecución

- JDK 21.
- PostgreSQL 18 o compatible.
- Una base de datos vacía llamada `transmetro_operativo` cargada con los scripts de la fase 4.

## Configuración local

La contraseña no se guarda en el proyecto. En PowerShell, defínala antes de iniciar la aplicación:

```powershell
$env:TRANSMETRO_DB_PASSWORD = "SU_CONTRASENA"
```

Si su usuario o base de datos es diferente, configure también `TRANSMETRO_DB_USER` y `TRANSMETRO_DB_URL`.

Defina además un usuario y contraseña de acceso. No tienen valores predeterminados para evitar publicar la aplicación sin protección:

```powershell
$env:APP_USERNAME = "operador"
$env:APP_PASSWORD = "UseUnaContrasenaLargaYUnica"
```

Ejecute una sola vez los scripts originales, en este orden, sobre la base vacía:

```powershell
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -d transmetro_operativo -f "..\outputs\2026-09-26\fase_4_modelado\10_Script_Base_de_Datos.sql"
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -d transmetro_operativo -f "..\outputs\2026-09-26\fase_4_modelado\10a_Datos_Demostracion.sql"
```

## Ejecución local con IntelliJ IDEA

1. Abra la carpeta `proyecto-transmetro` como proyecto Maven.
2. Configure las variables de entorno de la sección anterior en la configuración de ejecución.
3. Ejecute la clase `TransmetroOperativoApplication`.

Abra `http://localhost:8080`. La aplicación valida el esquema existente; no crea ni modifica tablas automáticamente.

## Despliegue de demostración en Render

El repositorio incluye un `Dockerfile` que compila la aplicación con Maven y la ejecuta con Java 21. Antes de crear el servicio web:

1. Publique el proyecto en GitHub o GitLab. No publique las contraseñas de PostgreSQL.
2. En Render, cree una base de datos PostgreSQL y ejecute los scripts `10_Script_Base_de_Datos.sql` y `10a_Datos_Demostracion.sql` sobre ella.
3. Cree un servicio web desde el repositorio, seleccione el entorno `Docker` y elija el plan disponible para demostraciones.
4. Configure estas variables de entorno en el servicio:

```text
TRANSMETRO_DB_URL=jdbc:postgresql://HOST:PUERTO/BASE_DE_DATOS
TRANSMETRO_DB_USER=USUARIO
TRANSMETRO_DB_PASSWORD=CONTRASENA
APP_USERNAME=USUARIO_DE_ACCESO
APP_PASSWORD=CONTRASENA_LARGA_Y_UNICA
```

Render define la variable `PORT`; la aplicación la usa automáticamente. Todos los recursos requieren iniciar sesión y los registros usan protección CSRF. La instancia limita las conexiones HTTP y de PostgreSQL para ajustarse a una demostración pequeña. Esto reduce el riesgo de abuso accidental o de registros no autorizados, pero no sustituye la protección de red contra ataques de denegación de servicio a gran escala.
