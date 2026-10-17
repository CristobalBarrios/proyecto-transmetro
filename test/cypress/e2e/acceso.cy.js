describe('Acceso y navegación', () => {
  beforeEach(() => cy.task('db:reset'));
  it('ACC-01 impide acceso sin iniciar sesión', () => {
    cy.visit('/');
    cy.location('pathname').should('eq', '/login');
  });
  it('ACC-02 informa credenciales incorrectas', () => {
    cy.visit('/login');
    cy.get('#csrf-token').should('not.have.value', '');
    cy.get('[name=username]').type('usuario.inexistente');
    cy.get('[name=password]').type('incorrecta', { log: false });
    cy.get('#login-form').submit();
    cy.get('#login-message').should('contain', 'Usuario o contraseña incorrectos');
  });
  it('ACC-03 permite mostrar y ocultar la contraseña', () => {
    cy.visit('/login');
    cy.get('#password').should('have.attr', 'type', 'password');
    cy.get('#password-toggle').click();
    cy.get('#password').should('have.attr', 'type', 'text');
    cy.get('#password-toggle').click();
    cy.get('#password').should('have.attr', 'type', 'password');
  });
  it('NAV-01 muestra una sola pantalla por entidad y conserva la ruta', () => {
    cy.ingresar();
    ['red/lineas', 'red/estaciones', 'flota/buses', 'personal/educacion'].forEach(route => {
      cy.get(`[data-route="${route}"]`).click();
      cy.get('[data-view]:visible').should('have.length', 1).and('have.attr', 'data-view', route);
      cy.location('hash').should('eq', `#/${route}`);
    });
    cy.reload();
    cy.get('[data-view]:visible').should('have.attr', 'data-view', 'personal/educacion');
  });
  it('SEG-01 rechaza una escritura sin protección CSRF', () => {
    cy.ingresar();
    cy.request({ method: 'POST', url: '/api/municipalidades', body: { nombre: 'No autorizado' }, failOnStatusCode: false })
      .its('status').should('eq', 403);
  });
});
