describe('Rutas de líneas', () => {
  beforeEach(() => { cy.task('db:reset'); cy.ingresar(); });
  it('RUT-01 guarda el orden y calcula la distancia total', () => {
    cy.get('[data-route="red/rutas"]').click();
    cy.get('#ruta-linea').select('1');
    cy.get('#ruta-editar').should('be.enabled').click();
    cy.get('#ruta-agregar').click();
    cy.get('.route-editor-row').eq(0).find('select').select('1');
    cy.get('#ruta-agregar').click();
    cy.get('.route-editor-row').eq(1).find('select').select('2');
    cy.get('.route-editor-row').eq(1).find('input').type('1.250');
    cy.get('#ruta-form').submit();
    cy.get('#ruta-list tr').should('have.length', 2);
    cy.get('#ruta-list tr').first().should('contain', 'Estación A').and('contain', 'ACC-A');
    cy.get('#ruta-resumen').should('contain', '1.250 km');
    cy.request('/api/rutas/1').then(({ body }) => {
      expect(body.map(item => item.estacionId)).to.deep.equal([1, 2]);
      expect(body.map(item => item.orden)).to.deep.equal([1, 2]);
    });
  });
  it('RUT-02 reordena y conserva la configuración después de recargar', () => {
    cy.escribirApi('PUT', '/api/rutas/1', { estaciones: [{ estacionId: 1, distanciaKm: 0 }, { estacionId: 2, distanciaKm: 1.25 }] });
    cy.get('[data-route="red/rutas"]').click();
    cy.get('#ruta-linea').select('1');
    cy.get('#ruta-editar').should('be.enabled').click();
    cy.get('.route-editor-row').eq(1).contains('button', 'Subir').click();
    cy.get('.route-editor-row').eq(1).find('input').clear().type('2.500');
    cy.get('#ruta-form').submit();
    cy.get('#ruta-list tr').first().should('contain', 'Estación B');
    cy.reload();
    cy.get('#ruta-linea').select('1');
    cy.get('#ruta-list tr').first().should('contain', 'Estación B');
    cy.get('#ruta-resumen').should('contain', '2.500 km');
  });
  it('RUT-03 rechaza duplicados sin perder la ruta anterior', () => {
    cy.escribirApi('PUT', '/api/rutas/1', { estaciones: [{ estacionId: 1, distanciaKm: 0 }] });
    cy.escribirApi('PUT', '/api/rutas/1', { estaciones: [{ estacionId: 1, distanciaKm: 0 }, { estacionId: 1, distanciaKm: 1 }] }, false)
      .its('status').should('eq', 400);
    cy.request('/api/rutas/1').its('body').should('have.length', 1);
  });
});
