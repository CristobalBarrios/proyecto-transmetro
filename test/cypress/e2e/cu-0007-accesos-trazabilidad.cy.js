describe('CU-0007 Administrar accesos y trazabilidad', () => {
  beforeEach(() => cy.task('db:reset'));
  it('CP-0007-05 registra un operador e inicia sesión en su estación', () => {
    cy.ingresar();
    cy.get('[data-route="administracion/usuarios"]').click();
    cy.get('#usuarios-content').contains('button', 'Nuevo usuario').click();
    cy.get('#usuarios-form [name=nombre]').type('Operador de estación');
    cy.get('#usuarios-form [name=nombreUsuario]').type('operador.estacion');
    cy.get('#usuarios-form [name=contrasena]').type('Clave-local-2026', { log: false });
    cy.get('#usuarios-form [name=perfil]').select('operador');
    cy.get('#usuarios-form [name=estacionId]').select('1');
    cy.get('#usuarios-form').submit();
    cy.get('#usuarios-list').should('contain', 'operador.estacion');
    cy.escribirApi('POST', '/logout');
    cy.visit('/login');
    cy.get('#csrf-token').should('not.have.value', '');
    cy.get('[name=username]').type('operador.estacion');
    cy.get('[name=password]').type('Clave-local-2026', { log: false });
    cy.get('#login-form').submit();
    cy.request('/api/sesion').its('body').should('include', { perfil: 'operador', estacionId: 1 });
    cy.get('[data-route="administracion/usuarios"]').should('not.be.visible');
  });
  it('CP-0007-06 consulta la trazabilidad de un cambio', () => {
    cy.ingresar();
    cy.escribirApi('POST', '/api/municipalidades', { nombre: 'Municipalidad auditada' });
    cy.reload();
    cy.get('[data-route="administracion/bitacora"]').click();
    cy.get('#bitacora-list').should('contain', 'municipalidad').and('contain', 'guardar');
  });
  it('CP-0007-01 permite ingresar con las credenciales de pruebas', () => {
    cy.ingresar();
    cy.get('[data-view]:visible').should('have.attr', 'data-view', 'inicio');
  });

  it('CP-0007-02 registra y modifica un acceso', () => {
    cy.ingresar();
    const datos = { estacionId: 2, codigo: 'ACC-B', descripcion: 'Acceso B' };
    cy.escribirApi('POST', '/api/accesos', datos).then(({ body }) => {
      cy.escribirApi('PUT', `/api/accesos/${body.id}`, datos).its('status').should('eq', 200);
    });
    cy.request('/api/accesos').then(({ body }) => expect(JSON.stringify(body)).to.contain('ACC-B'));
  });

  it('CP-0007-03 asigna un guardia y conserva fecha y responsable', () => {
    cy.ingresar();
    cy.escribirApi('POST', '/api/guardias', { nombre: 'Guardia de prueba', telefono: '12345678' });
    cy.reload();
    cy.get('[data-route="seguridad/asignaciones"]').click();
    cy.get('#asignacion-nueva').click();
    cy.get('#asignacion-modal').should('have.focus');
    cy.get('#asignacion-guardia').select('1');
    cy.get('#asignacion-acceso').select('1');
    cy.get('#asignacion-form').submit();
    cy.get('#asignaciones-list').should('contain', 'Guardia de prueba').and('contain', 'activa');
    cy.env(['username'], { log: false }).then(({ username }) => cy.get('#asignaciones-list').should('contain', username));
    cy.reload();
    cy.request('/api/asignaciones-guardias').its('body.0.fechaInicio').should('be.a', 'string');
    cy.get('#asignaciones-list').should('contain', 'ACC-A');
  });

  it('CP-0007-04 finaliza una asignación y conserva el historial', () => {
    cy.ingresar();
    cy.escribirApi('POST', '/api/guardias', { nombre: 'Guardia de prueba' });
    cy.escribirApi('POST', '/api/asignaciones-guardias', { guardiaId: 1, accesoId: 1 });
    cy.reload();
    cy.get('[data-route="seguridad/asignaciones"]').click();
    cy.get('#asignaciones-list').contains('button', 'Finalizar').click();
    cy.get('#asignaciones-list').should('contain', 'finalizada');
    cy.reload();
    cy.request('/api/asignaciones-guardias').its('body.0.fechaFin').should('be.a', 'string');
  });
});
