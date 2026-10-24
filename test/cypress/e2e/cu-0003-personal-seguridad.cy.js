describe('CU-0003 Administrar personal operativo y seguridad', () => {
  beforeEach(() => { cy.task('db:reset'); cy.ingresar(); });
  it('CP-0003-01 registra y modifica un piloto', () => {
    const datos = { nombre: 'Piloto nuevo', residencia: 'Residencia', telefono: '12345678', correo: null };
    cy.escribirApi('POST', '/api/pilotos', datos).then(({ body }) => {
      cy.escribirApi('PUT', `/api/pilotos/${body.id}`, datos).its('status').should('eq', 200);
    });
    cy.request('/api/pilotos').then(({ body }) => expect(JSON.stringify(body)).to.contain('Piloto nuevo'));
  });

  it('CP-0003-02 registra y modifica un guardia', () => {
    const datos = { nombre: 'Guardia nuevo', telefono: '12345678' };
    cy.escribirApi('POST', '/api/guardias', datos).then(({ body }) => {
      cy.escribirApi('PUT', `/api/guardias/${body.id}`, datos).its('status').should('eq', 200);
    });
    cy.request('/api/guardias').then(({ body }) => expect(JSON.stringify(body)).to.contain('Guardia nuevo'));
  });

  it('CP-0003-03 registra y modifica la formación de un piloto', () => {
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
});
