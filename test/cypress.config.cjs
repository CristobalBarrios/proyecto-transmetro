const { defineConfig } = require('cypress');
const { reiniciar } = require('./helpers/base-datos.cjs');
module.exports = defineConfig({
  allowCypressEnv: false,
  video: false,
  retries: 0,
  reporter: 'junit',
  reporterOptions: { mochaFile: 'resultados/cypress-[hash].xml' },
  e2e: {
    baseUrl: 'http://127.0.0.1:8080',
    setupNodeEvents(on) { on('task', { 'db:reset': reiniciar }); }
  },
  experimentalRunAllSpecs: true
});
