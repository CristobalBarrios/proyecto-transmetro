-- PostgreSQL. Ejecutar el archivo completo en pgAdmin sobre la base elegida.
-- ATENCIÓN: elimina todos los datos de la aplicación, salvo las cuentas
-- administradoras y sus contraseñas. No modifica tablas ni el esquema.
-- Detenga la aplicación mientras se ejecuta para evitar escrituras concurrentes.
-- Antes debe haber iniciado la versión actual al menos una vez, conectada a
-- esta misma base, con APP_USERNAME y APP_PASSWORD, para crear su administrador.
-- Si pgAdmin indica 25P02, ejecute ROLLBACK antes de volver a ejecutar el archivo.
-- Líneas y estaciones: nombres y direcciones publicados por la Municipalidad.
-- Fuente consultada el 24/10/2026:
-- https://www.muniguate.com/movilidadurbana/transmetro/
-- https://www.muniguate.com/movilidadurbana/linea-1/
-- https://www.muniguate.com/movilidadurbana/linea-2/
-- https://www.muniguate.com/movilidadurbana/linea-13/
-- Direcciones: mapa municipal FEB24-Transmetro.pdf, febrero de 2024.
-- https://www.muniguate.com/movilidadurbana/wp-content/uploads/sites/33/2024/02/FEB24-Transmetro.pdf
-- Se carga una MUESTRA de estaciones, no las rutas completas ni su distancia real.
-- Orden operativo, distancias, códigos internos, accesos, parqueos, personal,
-- buses y operaciones son datos, no información institucional.
-- Los códigos TM-* son identificadores internos de esta aplicación.
-- Los teléfonos son ficticios y los correos usan el dominio reservado .invalid.

BEGIN;
SET LOCAL TIME ZONE 'America/Guatemala';

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM usuario WHERE perfil = 'administrador' AND activo) THEN
        RAISE EXCEPTION 'No existe un administrador activo en la base %. Inicie una vez la versión actual de la aplicación conectada a esta base, con APP_USERNAME y APP_PASSWORD; después deténgala y vuelva a ejecutar este archivo. No se eliminó ningún dato.', current_database();
    END IF;
END $$;

CREATE TEMP TABLE demo_administradores ON COMMIT DROP AS
SELECT id, nombre, nombre_usuario, password_hash, perfil, activo
FROM usuario WHERE perfil = 'administrador';

TRUNCATE bitacora, evento_operativo, alerta, visita_estacion, recorrido,
    guardia_acceso, usuario, guardia, educacion_piloto, piloto, bus, parqueo,
    acceso, linea_estacion, estacion, linea, municipalidad RESTART IDENTITY;

INSERT INTO usuario (id, nombre, nombre_usuario, password_hash, perfil, activo)
SELECT id, nombre, nombre_usuario, password_hash, perfil, activo
FROM demo_administradores;
SELECT setval(pg_get_serial_sequence('usuario', 'id'), (SELECT max(id) FROM usuario));

INSERT INTO municipalidad (nombre)
VALUES ('Municipalidad de Guatemala');

INSERT INTO linea (municipalidad_id, codigo, nombre)
SELECT m.id, v.codigo, v.nombre
FROM (VALUES
    ('Municipalidad de Guatemala', '1', 'Línea 1'),
    ('Municipalidad de Guatemala', '2', 'Línea 2'),
    ('Municipalidad de Guatemala', '13', 'Línea 13')
) AS v(municipalidad, codigo, nombre)
JOIN municipalidad m ON m.nombre = v.municipalidad;

INSERT INTO estacion (municipalidad_id, nombre, direccion, zona, codigo_ruta)
SELECT m.id, v.nombre, v.direccion, v.zona, v.codigo
FROM (VALUES
    ('Municipalidad de Guatemala', 'San Sebastián', '3a calle y 6a avenida, zona 1', 1, 'TM-SSB'),
    ('Municipalidad de Guatemala', 'Mercado Central', '8a avenida y 6a calle, zona 1', 1, 'TM-MCE'),
    ('Municipalidad de Guatemala', 'Correos', '8a avenida y 12 calle, zona 1', 1, 'TM-COR'),
    ('Municipalidad de Guatemala', 'Hipódromo del Norte', 'Avenida Simeón Cañas y 11 calle, zona 2', 2, 'TM-HIP'),
    ('Municipalidad de Guatemala', 'Simeón Cañas', 'Avenida Simeón Cañas y 7a calle, zona 2', 2, 'TM-SCA'),
    ('Municipalidad de Guatemala', 'San José de la Montaña', 'Avenida Simeón Cañas y 6a calle, zona 2', 2, 'TM-SJM'),
    ('Municipalidad de Guatemala', 'Jocotenango', '6a avenida entre 3a y 4a calle, zona 2', 2, 'TM-JOC'),
    ('Municipalidad de Guatemala', 'Terminal', '6a avenida y 2a calle, zona 9', 9, 'TM-TER'),
    ('Municipalidad de Guatemala', 'Industria', '6a avenida y 6a calle, zona 9', 9, 'TM-IND'),
    ('Municipalidad de Guatemala', 'Tívoli', '6a avenida y 10a calle, zona 9', 9, 'TM-TIV')
) AS v(municipalidad, nombre, direccion, zona, codigo)
JOIN municipalidad m ON m.nombre = v.municipalidad;

-- San Sebastián figura como estación de transbordo en las fuentes municipales.
-- La secuencia siguiente es operativa para esta muestra; las distancias son simuladas.
INSERT INTO linea_estacion (linea_id, estacion_id, orden, distancia_desde_anterior_km)
SELECT l.id, e.id, v.orden, v.km
FROM (VALUES
    ('1', 'TM-SSB', 1, 0.000), ('1', 'TM-MCE', 2, 0.650), ('1', 'TM-COR', 3, 0.800),
    ('2', 'TM-HIP', 1, 0.000), ('2', 'TM-SCA', 2, 0.500), ('2', 'TM-SJM', 3, 0.200),
    ('2', 'TM-JOC', 4, 0.700), ('2', 'TM-SSB', 5, 0.450),
    ('13', 'TM-TER', 1, 0.000), ('13', 'TM-IND', 2, 0.650), ('13', 'TM-TIV', 3, 0.700)
) AS v(linea, estacion, orden, km)
JOIN linea l ON l.codigo = v.linea JOIN estacion e ON e.codigo_ruta = v.estacion;

INSERT INTO acceso (estacion_id, codigo, descripcion)
SELECT e.id, v.codigo, v.descripcion
FROM (VALUES
    ('TM-SSB', 'CEN-N', 'Ingreso norte'), ('TM-SSB', 'CEN-S', 'Ingreso sur'),
    ('TM-MCE', 'CIV-P', 'Ingreso principal'),
    ('TM-COR', 'TER-N', 'Ingreso norte'), ('TM-COR', 'TER-S', 'Ingreso sur'),
    ('TM-HIP', 'ORI-P', 'Ingreso principal'), ('TM-SCA', 'EMP-P', 'Ingreso principal'),
    ('TM-SJM', 'SUR-P', 'Ingreso principal'), ('TM-JOC', 'MER-P', 'Ingreso principal'),
    ('TM-TER', 'TSU-P', 'Ingreso principal')
) AS v(estacion, codigo, descripcion) JOIN estacion e ON e.codigo_ruta = v.estacion;

INSERT INTO parqueo (estacion_id, nombre)
SELECT e.id, v.nombre FROM (VALUES
    ('TM-SSB', 'Patio Central'), ('TM-SCA', 'Patio Oriente'), ('TM-TER', 'Patio Sur')
) AS v(estacion, nombre) JOIN estacion e ON e.codigo_ruta = v.estacion;

-- Cinco buses por línea: muestra de 3 estaciones en líneas 1 y 13, y 5 en línea 2.
-- Las cantidades cumplen el rango del enunciado para esta muestra.
-- El último bus tiene parqueo, pero no está asignado a una línea.
INSERT INTO bus (placa, capacidad_maxima, linea_id, parqueo_id, estado)
SELECT v.placa, v.capacidad, l.id, p.id, v.estado
FROM (VALUES
    ('DEMO-101', 100, '1', 'Patio Central', 'disponible'),
    ('DEMO-102', 100, '1', 'Patio Central', 'disponible'),
    ('DEMO-103', 100, '1', 'Patio Central', 'disponible'),
    ('DEMO-104', 100, '1', 'Patio Central', 'disponible'),
    ('DEMO-105', 80, '1', 'Patio Central', 'mantenimiento'),
    ('DEMO-201', 100, '2', 'Patio Oriente', 'disponible'),
    ('DEMO-202', 100, '2', 'Patio Oriente', 'disponible'),
    ('DEMO-203', 100, '2', 'Patio Oriente', 'disponible'),
    ('DEMO-204', 100, '2', 'Patio Oriente', 'disponible'),
    ('DEMO-205', 120, '2', 'Patio Oriente', 'disponible'),
    ('DEMO-301', 100, '13', 'Patio Sur', 'disponible'),
    ('DEMO-302', 100, '13', 'Patio Sur', 'disponible'),
    ('DEMO-303', 100, '13', 'Patio Sur', 'disponible'),
    ('DEMO-304', 100, '13', 'Patio Sur', 'disponible'),
    ('DEMO-305', 80, '13', 'Patio Sur', 'fuera_servicio'),
    ('DEMO-401', 100, NULL, 'Patio Central', 'disponible')
) AS v(placa, capacidad, linea, parqueo, estado)
LEFT JOIN linea l ON l.codigo = v.linea JOIN parqueo p ON p.nombre = v.parqueo;

INSERT INTO piloto (nombre, residencia, telefono, correo) VALUES
    ('Juan Esteban Morales', 'Colonia Los Pinos, zona 7, Guatemala', '0000-0101', 'juan.morales@example.invalid'),
    ('María Fernanda López', 'Colonia Las Flores, zona 12, Guatemala', '0000-0102', 'maria.lopez@example.invalid'),
    ('Carlos Enrique Méndez', 'Colonia El Prado, zona 5, Guatemala', '0000-0103', 'carlos.mendez@example.invalid'),
    ('Ana Lucía Ramírez', 'Residenciales del Sur, zona 4, Villa Nueva', '0000-0104', 'ana.ramirez@example.invalid'),
    ('Luis Fernando Castillo', 'Colonia La Esperanza, zona 3, Villa Nueva', '0000-0105', 'luis.castillo@example.invalid'),
    ('Sofía Isabel Herrera', 'Colonia Santa Elena, zona 11, Guatemala', '0000-0106', 'sofia.herrera@example.invalid');

INSERT INTO educacion_piloto (piloto_id, nivel, institucion, titulo, anio_finalizacion)
SELECT p.id, v.nivel, v.institucion, v.titulo, v.anio
FROM (VALUES
    ('Juan Esteban Morales', 'Diversificado', 'Instituto Técnico del Centro', 'Bachiller en Ciencias y Letras', 2013),
    ('María Fernanda López', 'Diversificado', 'Colegio Valle Verde', 'Perito Contador', 2016),
    ('Carlos Enrique Méndez', 'Diversificado', 'Instituto Técnico del Centro', 'Bachiller Industrial', 2011),
    ('Ana Lucía Ramírez', 'Diversificado', 'Instituto del Sur', 'Bachiller en Ciencias y Letras', 2017),
    ('Luis Fernando Castillo', 'Diversificado', 'Instituto del Sur', 'Bachiller Industrial', 2014),
    ('Sofía Isabel Herrera', 'Diversificado', 'Colegio Valle Verde', 'Bachiller en Ciencias y Letras', 2018),
    ('Juan Esteban Morales', 'Formación complementaria', 'Centro de Formación Vial', 'Conducción preventiva', 2024),
    ('María Fernanda López', 'Formación complementaria', 'Centro de Formación Vial', 'Atención al usuario', 2025)
) AS v(piloto, nivel, institucion, titulo, anio) JOIN piloto p ON p.nombre = v.piloto;

INSERT INTO guardia (nombre, telefono) VALUES
    ('Roberto Daniel Pérez', '0000-0201'), ('Marta Elena Ruiz', '0000-0202'),
    ('Diego Alejandro Santos', '0000-0203'), ('Patricia Isabel Cruz', '0000-0204'),
    ('José Antonio Aguilar', '0000-0205'), ('Claudia Beatriz León', '0000-0206'),
    ('Miguel Ángel Rosales', '0000-0207'), ('Gabriela Noemí Soto', '0000-0208'),
    ('Óscar Eduardo Rivas', '0000-0209'), ('Silvia Lorena Fuentes', '0000-0210');

-- No se publica ninguna contraseña predeterminada. Estas cuentas representan
-- operadores asignados. Para iniciar sesión con ellas, establezca su contraseña
-- desde Administración > Usuarios con la cuenta administradora conservada.
INSERT INTO usuario (nombre, nombre_usuario, password_hash, perfil, estacion_id)
SELECT v.nombre, v.usuario, '!CONFIGURAR_DESDE_ADMINISTRACION', 'Operador', e.id
FROM (VALUES
    ('Daniela Paola Reyes', 'operador.sansebastian', 'TM-SSB'),
    ('Andrés Felipe Guerra', 'operador.mercadocentral', 'TM-MCE'),
    ('Valeria Alejandra Paz', 'operador.correos', 'TM-COR'),
    ('Fernando José Cabrera', 'operador.hipodromo', 'TM-HIP'),
    ('Camila Andrea Flores', 'operador.simeoncanas', 'TM-SCA'),
    ('Ricardo Manuel Salazar', 'operador.sanjose', 'TM-SJM'),
    ('Elena Marisol Vargas', 'operador.jocotenango', 'TM-JOC'),
    ('Pablo Sebastián Duarte', 'operador.terminal9', 'TM-TER'),
    ('Lucía Fernanda Ovalle', 'operador.industria', 'TM-IND'),
    ('Héctor Raúl Estrada', 'operador.tivoli', 'TM-TIV')
) AS v(nombre, usuario, estacion) JOIN estacion e ON e.codigo_ruta = v.estacion;

-- Un usuario por cada perfil adicional; el administrador existente se conserva.
-- Configure contraseñas desde Administración > Usuarios antes de usarlos.
INSERT INTO usuario (nombre, nombre_usuario, password_hash, perfil) VALUES
    ('Supervisor', 'supervisor.demo', '!CONFIGURAR_DESDE_ADMINISTRACION', 'supervisor'),
    ('Responsable de flota', 'flota.demo', '!CONFIGURAR_DESDE_ADMINISTRACION', 'flota'),
    ('Responsable de estaciones', 'estaciones.demo', '!CONFIGURAR_DESDE_ADMINISTRACION', 'estaciones'),
    ('Responsable de seguridad', 'seguridad.demo', '!CONFIGURAR_DESDE_ADMINISTRACION', 'seguridad'),
    ('Responsable de personal', 'personal.demo', '!CONFIGURAR_DESDE_ADMINISTRACION', 'personal');

INSERT INTO guardia_acceso (guardia_id, acceso_id, asignado_por_usuario_id, fecha_inicio)
SELECT g.id, a.id, (SELECT min(id) FROM usuario WHERE perfil = 'administrador' AND activo),
    CURRENT_TIMESTAMP - interval '7 days'
FROM (VALUES
    ('Roberto Daniel Pérez', 'CEN-N'), ('Marta Elena Ruiz', 'CEN-S'),
    ('Diego Alejandro Santos', 'CIV-P'), ('Patricia Isabel Cruz', 'TER-N'),
    ('José Antonio Aguilar', 'TER-S'), ('Claudia Beatriz León', 'ORI-P'),
    ('Miguel Ángel Rosales', 'EMP-P'), ('Gabriela Noemí Soto', 'SUR-P'),
    ('Óscar Eduardo Rivas', 'MER-P'), ('Silvia Lorena Fuentes', 'TSU-P')
) AS v(guardia, acceso) JOIN guardia g ON g.nombre = v.guardia JOIN acceso a ON a.codigo = v.acceso;

-- Dos recorridos finalizados ayer y uno en curso desde hace 45 minutos.
INSERT INTO recorrido (linea_id, bus_id, piloto_id, inicio, fin)
SELECT l.id, b.id, p.id, v.inicio, v.fin FROM (VALUES
    ('1', 'DEMO-101', 'Juan Esteban Morales',
        (CURRENT_DATE - 1 + time '08:00')::timestamptz, (CURRENT_DATE - 1 + time '08:35')::timestamptz),
    ('2', 'DEMO-201', 'María Fernanda López',
        (CURRENT_DATE - 1 + time '09:00')::timestamptz, (CURRENT_DATE - 1 + time '09:40')::timestamptz),
    ('1', 'DEMO-102', 'Carlos Enrique Méndez', CURRENT_TIMESTAMP - interval '45 minutes', NULL::timestamptz)
) AS v(linea, bus, piloto, inicio, fin)
JOIN linea l ON l.codigo = v.linea JOIN bus b ON b.placa = v.bus JOIN piloto p ON p.nombre = v.piloto;

INSERT INTO visita_estacion (recorrido_id, estacion_id, orden_programado, llegada,
    salida_prevista, salida, pasajeros_abordo, pasajeros_esperando, capacidad_bus, usuario_id)
SELECT r.id, e.id, le.orden, v.llegada, v.prevista, v.salida, v.abordo, v.esperando,
    b.capacidad_maxima, u.id
FROM (VALUES
    ('DEMO-101', 'TM-SSB', (CURRENT_DATE - 1 + time '08:00')::timestamptz,
        (CURRENT_DATE - 1 + time '08:02')::timestamptz, (CURRENT_DATE - 1 + time '08:07')::timestamptz, 24, 150),
    ('DEMO-101', 'TM-MCE', (CURRENT_DATE - 1 + time '08:15')::timestamptz,
        (CURRENT_DATE - 1 + time '08:17')::timestamptz, (CURRENT_DATE - 1 + time '08:17')::timestamptz, 58, 35),
    ('DEMO-101', 'TM-COR', (CURRENT_DATE - 1 + time '08:30')::timestamptz,
        (CURRENT_DATE - 1 + time '08:32')::timestamptz, (CURRENT_DATE - 1 + time '08:32')::timestamptz, 35, 18),
    ('DEMO-201', 'TM-HIP', (CURRENT_DATE - 1 + time '09:00')::timestamptz,
        (CURRENT_DATE - 1 + time '09:02')::timestamptz, (CURRENT_DATE - 1 + time '09:02')::timestamptz, 42, 60),
    ('DEMO-201', 'TM-SCA', (CURRENT_DATE - 1 + time '09:08')::timestamptz,
        (CURRENT_DATE - 1 + time '09:10')::timestamptz, (CURRENT_DATE - 1 + time '09:10')::timestamptz, 76, 40),
    ('DEMO-201', 'TM-SJM', (CURRENT_DATE - 1 + time '09:15')::timestamptz,
        (CURRENT_DATE - 1 + time '09:17')::timestamptz, (CURRENT_DATE - 1 + time '09:17')::timestamptz, 51, 25),
    ('DEMO-201', 'TM-JOC', (CURRENT_DATE - 1 + time '09:25')::timestamptz,
        (CURRENT_DATE - 1 + time '09:27')::timestamptz, (CURRENT_DATE - 1 + time '09:27')::timestamptz, 47, 20),
    ('DEMO-201', 'TM-SSB', (CURRENT_DATE - 1 + time '09:35')::timestamptz,
        (CURRENT_DATE - 1 + time '09:37')::timestamptz, (CURRENT_DATE - 1 + time '09:37')::timestamptz, 34, 12),
    ('DEMO-102', 'TM-SSB', CURRENT_TIMESTAMP - interval '40 minutes',
        CURRENT_TIMESTAMP - interval '38 minutes', CURRENT_TIMESTAMP - interval '37 minutes', 45, 30),
    ('DEMO-102', 'TM-MCE', CURRENT_TIMESTAMP - interval '20 minutes',
        CURRENT_TIMESTAMP - interval '18 minutes', NULL::timestamptz, 20, 160)
) AS v(bus, estacion, llegada, prevista, salida, abordo, esperando)
JOIN bus b ON b.placa = v.bus JOIN recorrido r ON r.bus_id = b.id
JOIN estacion e ON e.codigo_ruta = v.estacion
JOIN linea_estacion le ON le.linea_id = r.linea_id AND le.estacion_id = e.id
JOIN usuario u ON u.estacion_id = e.id AND u.perfil = 'operador';

-- La alerta histórica ya fue atendida; la del recorrido actual queda pendiente.
INSERT INTO alerta (visita_id, estado, bus_refuerzo_id, creada, atendida)
SELECT v.id, CASE WHEN r.fin IS NULL THEN 'pendiente' ELSE 'atendida' END,
    CASE WHEN r.fin IS NOT NULL THEN (SELECT id FROM bus WHERE placa = 'DEMO-103') END,
    v.llegada, CASE WHEN r.fin IS NOT NULL THEN v.llegada + interval '6 minutes' END
FROM visita_estacion v JOIN recorrido r ON r.id = v.recorrido_id WHERE v.solicita_refuerzo;

INSERT INTO evento_operativo (tipo, descripcion, fecha, linea_id, estacion_id, usuario_registra_id)
SELECT v.tipo, v.descripcion, CURRENT_TIMESTAMP - v.antiguedad, l.id, e.id, u.id
FROM (VALUES
    ('Señalización', 'Se reportó desgaste de señalización en el ingreso norte.', '1', 'TM-SSB', interval '2 days'),
    ('Iluminación', 'Se registró una luminaria intermitente en el área de espera.', '2', 'TM-HIP', interval '1 day'),
    ('Información al usuario', 'Se solicitó actualizar el panel informativo del andén principal.', '13', 'TM-TER', interval '2 hours')
) AS v(tipo, descripcion, linea, estacion, antiguedad)
JOIN linea l ON l.codigo = v.linea JOIN estacion e ON e.codigo_ruta = v.estacion
JOIN usuario u ON u.estacion_id = e.id AND u.perfil = 'operador';

INSERT INTO bitacora (usuario_id, accion, entidad, registro_id, fecha)
SELECT v.usuario_id, 'registrar', 'visita_estacion', v.id, v.llegada FROM visita_estacion v;
INSERT INTO bitacora (usuario_id, accion, entidad, registro_id, fecha)
SELECT usuario_registra_id, 'registrar', 'evento_operativo', id, fecha FROM evento_operativo;

COMMIT;

-- Resultado esperado: 1 municipalidad, 3 líneas, 10 estaciones, 16 buses,
-- 6 pilotos, 10 guardias, 10 operadores, 5 usuarios de otros perfiles y los
-- administradores conservados. Las líneas 1 y 2 comparten San Sebastián.
-- La línea 1 tiene un recorrido y una alerta pendiente.
-- DEMO-103 o DEMO-104 están disponibles para demostrar el refuerzo.
SELECT 'Municipalidades' AS entidad, count(*) AS registros FROM municipalidad
UNION ALL SELECT 'Líneas', count(*) FROM linea
UNION ALL SELECT 'Estaciones', count(*) FROM estacion
UNION ALL SELECT 'Buses', count(*) FROM bus
UNION ALL SELECT 'Pilotos', count(*) FROM piloto
UNION ALL SELECT 'Guardias', count(*) FROM guardia
UNION ALL SELECT 'Usuarios', count(*) FROM usuario
UNION ALL SELECT 'Recorridos', count(*) FROM recorrido
UNION ALL SELECT 'Visitas', count(*) FROM visita_estacion
UNION ALL SELECT 'Alertas', count(*) FROM alerta;
