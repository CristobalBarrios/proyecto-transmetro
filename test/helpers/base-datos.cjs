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
    await cliente.query('BEGIN');
    await cliente.query(`TRUNCATE bitacora, evento_operativo, alerta, visita_estacion, recorrido,
      guardia_acceso, usuario, guardia, educacion_piloto, piloto, bus, parqueo, acceso,
      linea_estacion, estacion, linea, municipalidad RESTART IDENTITY`);
    await cliente.query("INSERT INTO municipalidad (nombre) VALUES ('Municipalidad de prueba')");
    await cliente.query("INSERT INTO linea (municipalidad_id,codigo,nombre) VALUES (1,'PRUEBA-1','Línea de prueba')");
    await cliente.query(`INSERT INTO estacion (municipalidad_id,nombre,direccion,zona,codigo_ruta)
      VALUES (1,'Estación A','Dirección A',1,'PRUEBA-A'),(1,'Estación B','Dirección B',2,'PRUEBA-B')`);
    await cliente.query("INSERT INTO acceso (estacion_id,codigo) VALUES (1,'ACC-A')");
    await cliente.query("INSERT INTO parqueo (nombre,estacion_id) VALUES ('Parqueo de prueba',1)");
    await cliente.query("INSERT INTO piloto (nombre,residencia,telefono) VALUES ('Piloto de prueba','Residencia','00000000')");
    await cliente.query('COMMIT');
    return null;
  } catch (error) { await cliente.query('ROLLBACK'); throw error; }
  finally { await cliente.end(); }
}
module.exports = { config, provisionar, reiniciar };
