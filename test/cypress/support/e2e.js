Cypress.Commands.add('ingresar', () => {
  cy.visit('/login');
  cy.get('#csrf-token').should('not.have.value', '');
  cy.env(['username', 'password'], { log: false }).then(({ username, password }) => {
    if (!username || !password) throw new Error('Use npm.cmd --prefix test run test:e2e:open desde la carpeta de la aplicación para configurar las credenciales de pruebas.');
    cy.get('[name=username]').type(username, { log: false });
    cy.get('[name=password]').type(password, { log: false });
  });
  cy.get('#login-form').submit();
  cy.location('pathname').should('eq', '/');
  cy.get('#summary-cards .summary-card').should('have.length.greaterThan', 0);
});
Cypress.Commands.add('prepararLinea', () => {
  cy.escribirApi('POST', '/api/guardias', { nombre: 'Guardia de prueba', telefono: '12345678' });
  cy.escribirApi('POST', '/api/accesos', { estacionId: 2, codigo: 'ACC-B', descripcion: 'Acceso B' });
  cy.escribirApi('PUT', '/api/rutas/1', { estaciones: [{ estacionId: 1, distanciaKm: 0 }, { estacionId: 2, distanciaKm: 1.25 }] });
  ['BUS-A', 'BUS-B', 'BUS-C'].forEach(placa => cy.escribirApi('POST', '/api/buses', {
    placa, capacidadMaxima: 80, lineaId: 1, parqueoId: 1, estado: 'disponible'
  }));
  [1, 2].forEach(accesoId => cy.escribirApi('POST', '/api/asignaciones-guardias', { guardiaId: 1, accesoId }));
  [1, 2].forEach(estacionId => cy.escribirApi('POST', '/api/usuarios', {
    nombre: `Operador ${estacionId}`, nombreUsuario: `operador${estacionId}`, contrasena: 'Prueba-local-2026',
    perfil: 'operador', estacionId, activo: true
  }));
});
Cypress.Commands.add('escribirApi', (method, url, body, failOnStatusCode = true) => {
  cy.request('/api/csrf').then(({ body: token }) => cy.request({
    method, url, body, headers: { [token.encabezado]: token.valor }, failOnStatusCode
  }));
});

Cypress.Commands.add('iniciarRecorrido', () => {
  cy.escribirApi('PUT', '/api/operacion/lineas/1/estado', { estado: 'operativa' });
  return cy.escribirApi('POST', '/api/recorridos', { lineaId: 1, busId: 1, pilotoId: 1 });
});

Cypress.Commands.add('registrarVisita', (estacionId, pasajerosAbordo = 40, pasajerosEsperando = 0) => {
  return cy.escribirApi('POST', '/api/visitas', {
    recorridoId: 1, estacionId, pasajerosAbordo, pasajerosEsperando,
    llegada: '2026-10-01T12:00:00Z', salidaPrevista: '2026-10-01T12:01:00Z'
  });
});
