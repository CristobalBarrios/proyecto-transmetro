const { spawn } = require('node:child_process');
const fs = require('node:fs');
const path = require('node:path');
const { config, provisionar } = require('./base-datos.cjs');

async function main() {
  const project = path.resolve(__dirname, '..');
  process.chdir(project);
  const settings = config();
  const port = Number(process.env.E2E_APP_PORT || 8081);
  if (!Number.isInteger(port) || port < 1024 || port > 65535) throw new Error('Puerto de pruebas inválido.');
  const jar = path.resolve(__dirname, '../../target/transmetro-operativo-0.0.1-SNAPSHOT.jar');
  if (!fs.existsSync(jar)) throw new Error('Primero ejecute Maven package con Java 21.');
  // Rechaza un puerto ocupado para no probar otra instancia por accidente.
  const net = require('node:net');
  await new Promise((resolve, reject) => {
    const server = net.createServer(); server.once('error', reject);
    server.listen(port, '127.0.0.1', () => server.close(resolve));
  });
  await provisionar();
  const credentials = {
    username: process.env.E2E_USERNAME || 'operador.pruebas',
    password: process.env.E2E_PASSWORD || require('node:crypto').randomBytes(24).toString('hex')
  };
  process.env.E2E_USERNAME = credentials.username;
  const java = process.env.JAVA_HOME ? path.join(process.env.JAVA_HOME, 'bin', process.platform === 'win32' ? 'java.exe' : 'java') : 'java';
  const logs = path.resolve(project, 'resultados'); fs.mkdirSync(logs, { recursive: true });
  const output = fs.openSync(path.join(logs, 'servidor.log'), 'w');
  const server = spawn(java, ['-jar', jar, '--server.address=127.0.0.1'], {
    windowsHide: true, stdio: ['ignore', output, output],
    env: { ...process.env, PORT: String(port), APP_USERNAME: credentials.username, APP_PASSWORD: credentials.password,
      TRANSMETRO_DB_URL: `jdbc:postgresql://127.0.0.1:${settings.port}/${settings.database}`,
      TRANSMETRO_DB_USER: settings.user, TRANSMETRO_DB_PASSWORD: settings.password }
  });
  server.on('error', error => { console.error(error.message); });
  const baseUrl = `http://127.0.0.1:${port}`;
  const cleanup = () => { if (server.exitCode === null) server.kill(); };
  process.once('SIGINT', () => { cleanup(); process.exit(130); });
  try {
    let ready = false;
    for (let attempt = 0; attempt < 90; attempt++) {
      if (server.exitCode !== null) throw new Error('La aplicación no inició. Revise test/resultados/servidor.log.');
      try { ready = (await fetch(`${baseUrl}/login`, { signal: AbortSignal.timeout(1500) })).ok; } catch {}
      if (ready) break;
      await new Promise(resolve => setTimeout(resolve, 1000));
    }
    if (!ready) throw new Error('La aplicación no respondió dentro del plazo.');
    const cypress = require('cypress');
    const options = { project, config: { baseUrl }, env: credentials };
    if (process.argv.includes('--open')) await cypress.open(options);
    else {
      const result = await cypress.run(options);
      process.exitCode = result.totalFailed || result.failures ? 1 : 0;
    }
  } finally { cleanup(); fs.closeSync(output); }
}
main().catch(error => { console.error(error.message); process.exitCode = 1; });
