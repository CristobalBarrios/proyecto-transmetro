# Plan de Pruebas

Sistema de control operativo para Transmetro

Dirección de Movilidad Urbana

Municipalidad de Guatemala

Presento el plan para verificar los flujos principales del sistema de control operativo para Transmetro, conforme a los casos de uso del Análisis de Requerimientos del 5 de septiembre y al diseño del 26 de septiembre de 2026.

## Alcance y ejecución

Se verificará un flujo principal por funcionalidad: Java para reglas del servidor con datos simulados, Cypress para flujos y persistencia en PostgreSQL, y revisión manual de la interfaz. Se excluyen escenarios negativos, casos de borde, carga y seguridad especializada.

Cristóbal Barrios Larios ejecutará las pruebas con Java 21, PostgreSQL y Cypress en un ambiente local aislado, con datos ficticios y credenciales de prueba. Se requerirán las funciones y datos previos disponibles y fuentes autorizadas para las cargas. No se restablecerán datos de producción.

Se probará cada funcionalidad al completarla y se repetirá la regresión tras los cambios. La revisión y las correcciones se prevén antes del 24 de octubre de 2026. El representante municipal evaluará la aceptación.

## Pruebas por caso de uso

El identificador CP-NNNN-XX relaciona la prueba con CU-NNNN y su consecutivo XX. Cypress utilizará los mismos identificadores y un archivo por caso de uso. CP-TR identifica las revisiones manuales transversales.

### CU-0001 Administrar líneas y estaciones

| Prueba | Acción y resultado esperado | Método |
| --- | --- | --- |
| CP-0001-01 | Crear una línea, modificar su nombre y recargar. Los cambios permanecen guardados. | Cypress |
| CP-0001-02 | Crear una estación con dirección, zona y código de ruta. La tabla muestra sus valores. | Cypress |
| CP-0001-03 | Guardar dos estaciones en orden, con distancias de 0 y 1.250 km. Se conservan el orden y la distancia total. | Java y Cypress |

### CU-0002 Administrar flota y parqueos

| Prueba | Acción y resultado esperado | Método |
| --- | --- | --- |
| CP-0002-01 | Registrar y actualizar un parqueo. Conserva sus datos y la estación relacionada. | Cypress |
| CP-0002-02 | Registrar y actualizar un bus. Conserva placa, capacidad, estado, línea y parqueo. | Cypress |

### CU-0003 Administrar personal operativo y seguridad

| Prueba | Acción y resultado esperado | Método |
| --- | --- | --- |
| CP-0003-01 | Registrar y actualizar un piloto. Se conservan sus datos personales y de contacto. | Cypress |
| CP-0003-02 | Registrar y actualizar un guardia. Se conservan sus datos y queda disponible para asignación. | Cypress |
| CP-0003-03 | Registrar la formación de un piloto y agregar su título. La información permanece vinculada al piloto. | Java y Cypress |

### CU-0004 Registrar asignación operativa

| Prueba | Acción y resultado esperado | Método |
| --- | --- | --- |
| CP-0004-01 | Activar una línea con dos estaciones, tres buses y todos sus accesos cubiertos por guardias. Se guarda el estado operativa. | Java y Cypress |
| CP-0004-02 | Desactivar una línea sin recorridos en curso. Se guarda el estado inactiva. | Java y Cypress |
| CP-0004-03 | Modificar la capacidad de un bus sin cambiar su línea. La línea conserva su preparación operativa. | Java y Cypress |
| CP-0004-04 | Asignar bus y piloto disponibles e iniciar un recorrido. Se conservan línea, recursos y fecha de inicio. | Java y Cypress |
| CP-0004-05 | Finalizar un recorrido con sus visitas registradas. Se guarda la fecha de fin y se liberan sus recursos. | Java y Cypress |
| CP-0004-06 | Registrar y atender un evento operativo. Se conservan operación, responsable, estado y fechas. | Java y Cypress |

### CU-0005 Registrar ocupación y gestionar alertas

| Prueba | Acción y resultado esperado | Método |
| --- | --- | --- |
| CP-0005-01 | Registrar visitas en el orden de la ruta. Se conservan ocupación, fechas y recorrido asociado. | Java y Cypress |
| CP-0005-02 | Con capacidad de 100 y 20 pasajeros a bordo, completar la visita tras cinco minutos adicionales. Las fechas reflejan la espera. | Java y Cypress |
| CP-0005-03 | Registrar 200 pasajeros esperando ante un bus de capacidad 100. Se genera una alerta pendiente para la visita. | Java y Cypress |
| CP-0005-04 | Atender una alerta con un bus de refuerzo disponible. Se conservan el bus asignado y la fecha de atención. | Java y Cypress |

### CU-0006 Consultar reportes operativos

| Prueba | Acción y resultado esperado | Método |
| --- | --- | --- |
| CP-0006-01 | Consultar el reporte de red y flota. Líneas, estaciones, accesos, buses y distancias coinciden con los datos registrados. | Java y Cypress |
| CP-0006-02 | Consultar operaciones, personal, eventos y alertas por período y elemento operativo. Los registros y totales respetan los filtros. | Java y Cypress |

### CU-0007 Administrar accesos y trazabilidad

| Prueba | Acción y resultado esperado | Método |
| --- | --- | --- |
| CP-0007-01 | Iniciar sesión con credenciales válidas. Se accede a la pantalla inicial. | Cypress |
| CP-0007-02 | Registrar y actualizar un acceso. Conserva código, descripción y estación relacionada. | Cypress |
| CP-0007-03 | Asignar un guardia a un acceso. Se guardan estado activa, fecha de inicio y responsable. | Java y Cypress |
| CP-0007-04 | Finalizar una asignación de guardia de una línea no operativa. El historial conserva estado y fecha de fin. | Java y Cypress |
| CP-0007-05 | Registrar un operador e iniciar sesión. Accede a las funciones de su perfil y estación. | Java y Cypress |
| CP-0007-06 | Realizar un cambio y consultar su bitácora. Se registran usuario, fecha, acción, entidad y registro afectado. | Java y Cypress |

### CU-0008 Actualizar datos de referencia

| Prueba | Acción y resultado esperado | Método |
| --- | --- | --- |
| CP-0008-01 | Registrar y actualizar una municipalidad. El dato queda disponible para los catálogos relacionados. | Cypress |
| CP-0008-02 | Cargar o actualizar rutas y estaciones desde una fuente autorizada con datos válidos. Los registros quedan incorporados o actualizados. | Java y Cypress |

### Verificaciones transversales

| Prueba | Acción y resultado esperado | Método |
| --- | --- | --- |
| CP-TR-01 | Revisar los módulos en escritorio. Textos, tablas, colores e iconos son legibles y consistentes. | Manual |
| CP-TR-02 | Abrir tablas extensas y formularios. Las barras de navegación y los controles permanecen utilizables. | Manual |
| CP-TR-03 | Navegar en pantalla pequeña y con teclado. El menú, los campos y las acciones principales son accesibles. | Manual |

## Criterios de cierre

Todas las pruebas acordadas deberán cumplir su resultado esperado, sin defectos críticos o altos que comprometan datos o funciones principales. Se suspenderá la ejecución ante problemas del ambiente o los datos hasta corregirlos.

Los resultados e incidencias se registrarán por separado con prueba, versión y evidencia. La aceptación se formalizará mediante la Carta de Aceptación municipal y no acreditará los escenarios excluidos.

Cristóbal Barrios Larios

Consultor externo
