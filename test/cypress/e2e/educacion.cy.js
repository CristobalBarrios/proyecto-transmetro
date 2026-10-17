describe('Historial educativo', () => {
  beforeEach(() => { cy.task('db:reset'); cy.ingresar(); });
  it('EDU-01 registra y modifica la formación de un piloto', () => {
    cy.get('[data-route="personal/educacion"]').click();
    cy.get('#educacion-content').contains('button', 'Nueva formación').click();
    cy.get('#educacion-modal').should('have.focus');
    cy.get('#educacion-form [name=pilotoId]').select('1');
    cy.get('#educacion-form [name=nivel]').type('Diversificado');
    cy.get('#educacion-form [name=institucion]').type('Instituto de prueba');
    cy.get('#educacion-form [name=anioFinalizacion]').type('2020');
    cy.get('#educacion-form').submit();
    cy.get('#educacion-modal').should('have.css', 'display', 'none');
    cy.get('#educacion-list').contains('tr', 'Instituto de prueba').contains('button', 'Editar').click();
    cy.get('#educacion-modal').should('have.focus');
    cy.get('#educacion-form [name=titulo]').type('Bachiller');
    cy.get('#educacion-form').submit();
    cy.get('#educacion-list').should('contain', 'Bachiller').and('contain', 'Piloto de prueba');
    cy.reload();
    cy.get('#educacion-list').should('contain', 'Bachiller');
  });
  it('EDU-02 valida límites del año y campos obligatorios', () => {
    const base = { pilotoId: 1, nivel: 'Diversificado', institucion: 'Instituto', titulo: null };
    [1899, 2101].forEach(anio => cy.escribirApi('POST', '/api/educacion-pilotos', { ...base, anioFinalizacion: anio }, false)
      .its('status').should('eq', 400));
    cy.escribirApi('POST', '/api/educacion-pilotos', { ...base, nivel: '' }, false).its('status').should('eq', 400);
    [1900, 2100, null].forEach(anio => cy.escribirApi('POST', '/api/educacion-pilotos', { ...base, anioFinalizacion: anio })
      .its('status').should('eq', 201));
  });
});
