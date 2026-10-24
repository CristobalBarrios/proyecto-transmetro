const { Client } = require('pg');
const fs = require('node:fs');
const path = require('node:path');

function config() {
  const database = process.env.E2E_DB_NAME || 'transmetro_operativo_pruebas';
  if (!/^[a-z][a-z0-9_]*_pruebas$/.test(database)) throw new Error('La base debe terminar en _pruebas.');
  if (!process.env.E2E_DB_PASSWORD) throw new Error('Defina E2E_DB_PASSWORD.');
  return { host: '127.0.0.1', port: Number(process.env.E2E_DB_PORT || 5432), database,
    user: process.env.E2E_DB_USER || 'postgres', password: process.env.E2E_DB_PASSWORD };
}

async function provisionar() {
  const settings = config();
  const admin = new Client({ ...settings, database: 'postgres' });
  await admin.connect();
  try {
    const existe = await admin.query('SELECT 1 FROM pg_database WHERE datname = $1', [settings.database]);
    if (!existe.rowCount) await admin.query(`CREATE DATABASE "${settings.database}"`);
  } finally { await admin.end(); }
  const cliente = new Client(settings);
  await cliente.connect();
  try {
    const tabla = await cliente.query("SELECT to_regclass('public.municipalidad') AS tabla");
    if (!tabla.rows[0].tabla) {
      const esquema = path.resolve(__dirname, '../../../outputs/2026-09-26/fase_4_modelado/10_Script_Base_de_Datos.sql');
      await cliente.query(fs.readFileSync(esquema, 'utf8'));
    }
  } finally { await cliente.end(); }
}

async function reiniciar() {
  const cliente = new Client(config());
  await cliente.connect();
  try {
    const resultado = await cliente.query('SELECT current_database() AS nombre');
    if (resultado.rows[0].nombre !== config().database) throw new Error('Base de pruebas incorrecta.');
    const inicial = await cliente.query('SELECT nombre,nombre_usuario,password_hash FROM usuario WHERE nombre_usuario=$1', [process.env.E2E_USERNAME || 'operador.pruebas']);
    await cliente.query('BEGIN');
    await cliente.query(`TRUNCATE bitacora, evento_operativo, alerta, visita_estacion, recorrido,
      guardia_acceso, usuario, guardia, educacion_piloto, piloto, bus, parqueo, acceso,
      linea_estacion, estacion, linea, municipalidad RESTART IDENTITY`);
    if (inicial.rows.length) {
      const u = inicial.rows[0];
      await cliente.query("INSERT INTO usuario(nombre,nombre_usuario,password_hash,perfil) VALUES ($1,$2,$3,'administrador')", [u.nombre,u.nombre_usuario,u.password_hash]);
    }
    await cliente.query("INSERT INTO municipalidad (nombre) VALUES ('Municipalidad de prueba')");
    await cliente.query("INSERT INTO linea (municipalidad_id,codigo,nombre) VALUES (1,'PRUEBA-1','Línea de prueba')");
    await cliente.query(`INSERT INTO estacion (municipalidad_id,nombre,direccion,zona,codigo_ruta)
      VALUES (1,'Estación A','Dirección A',1,'PRUEBA-A'),(1,'Estación B','Dirección B',2,'PRUEBA-B')`);
    await cliente.query("INSERT INTO acceso (estacion_id,codigo) VALUES (1,'ACC-A')");
    await cliente.query("INSERT INTO parqueo (nombre,estacion_id) VALUES ('Parqueo de prueba',1)");
    await cliente.query("INSERT INTO piloto (nombre,residencia,telefono) VALUES ('Piloto de prueba','Residencia','00000000')");
    await cliente.query("INSERT INTO linea_estacion(linea_id,estacion_id,orden,distancia_desde_anterior_km) VALUES (1,1,1,0),(1,2,2,1.250)");
    await cliente.query("INSERT INTO bus(placa,capacidad_maxima,linea_id,parqueo_id) VALUES ('BUS-A',80,1,1),('BUS-B',80,1,1)");
    await cliente.query("INSERT INTO guardia(nombre) VALUES ('Guardia inicial')");
    await cliente.query("INSERT INTO guardia_acceso(guardia_id,acceso_id,asignado_por_usuario_id) SELECT 1,1,id FROM usuario WHERE perfil='administrador' ORDER BY id LIMIT 1");
    await cliente.query("INSERT INTO usuario(nombre,nombre_usuario,password_hash,perfil,estacion_id) VALUES ('Operador 1','operador1','!PRUEBA','operador',1),('Operador 2','operador2','!PRUEBA','operador',2)");
    await cliente.query('COMMIT');
    return null;
  } catch (error) { await cliente.query('ROLLBACK'); throw error; }
  finally { await cliente.end(); }
}
module.exports = { config, provisionar, reiniciar };
