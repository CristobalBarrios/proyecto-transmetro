describe('CU-0001 Administrar líneas y estaciones', () => {
  beforeEach(() => { cy.task('db:reset'); cy.ingresar(); });
  it('CP-0001-03 guarda y conserva la ruta y su distancia total', () => {
    cy.get('[data-route="red/rutas"]').click();
    cy.get('#ruta-linea').select('1');
    cy.get('#ruta-editar').click();
    cy.get('#ruta-agregar').click();
    cy.get('.route-editor-row').eq(0).find('select').select('1');
    cy.get('#ruta-agregar').click();
    cy.get('.route-editor-row').eq(1).find('select').select('2');
    cy.get('.route-editor-row').eq(1).find('input').type('1.250');
    cy.get('#ruta-form').submit();
    cy.get('#ruta-modal').should('not.be.visible');
    cy.reload();
    cy.get('#ruta-linea').select('1');
    cy.get('#ruta-list tr').should('have.length', 2);
    cy.get('#ruta-resumen').should('contain', '1.250 km');
  });
  it('CP-0001-01 crea, modifica y conserva una línea', () => {
    cy.get('[data-route="red/lineas"]').click();
    cy.get('#lineas-modal').should('not.be.visible');
    cy.get('#lineas-content').contains('button', 'Nueva línea').click();
    cy.get('#lineas-modal').should('have.focus');
    cy.get('#lineas-form [name=municipalidadId]').select('1');
    cy.get('#lineas-form [name=codigo]').type('PRUEBA-2');
    cy.get('#lineas-form [name=nombre]').type('Nueva línea de prueba');
    cy.get('#lineas-form').submit();
    cy.get('#lineas-modal').should('have.css', 'display', 'none');
    cy.get('#lineas-list').contains('tr', 'PRUEBA-2').contains('button', 'Editar').click();
    cy.get('#lineas-modal').should('have.focus');
    cy.get('#lineas-form [name=nombre]').clear().type('Línea modificada');
    cy.get('#lineas-form').submit();
    cy.get('#lineas-list').should('contain', 'Línea modificada');
    cy.reload();
    cy.get('#lineas-list').should('contain', 'Línea modificada');
  });
  it('CP-0001-02 crea una estación con todos sus campos', () => {
    cy.get('[data-route="red/estaciones"]').click();
    cy.get('#estaciones-content').contains('button', 'Nueva estación').click();
    cy.get('#estaciones-modal').should('have.focus');
    cy.get('#estaciones-form [name=municipalidadId]').select('1');
    cy.get('#estaciones-form [name=nombre]').type('Estación C');
    cy.get('#estaciones-form [name=direccion]').type('Dirección C');
    cy.get('#estaciones-form [name=zona]').type('3');
    cy.get('#estaciones-form [name=codigoRuta]').type('PRUEBA-C');
    cy.get('#estaciones-form').submit();
    cy.get('#estaciones-list').contains('tr', 'Estación C').should('contain', 'Dirección C').and('contain', 'PRUEBA-C');
  });
});
