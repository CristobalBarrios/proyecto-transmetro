describe('CU-0002 Administrar flota y parqueos', () => {
  beforeEach(() => { cy.task('db:reset'); cy.ingresar(); });

  it('CP-0002-01 registra y modifica un parqueo', () => {
    const datos = { nombre: 'Parqueo nuevo', estacionId: 1 };
    cy.escribirApi('POST', '/api/parqueos', datos).then(({ body }) => {
      cy.escribirApi('PUT', `/api/parqueos/${body.id}`, datos).its('status').should('eq', 200);
    });
    cy.request('/api/parqueos').then(({ body }) => expect(JSON.stringify(body)).to.contain('Parqueo nuevo'));
  });

  it('CP-0002-02 registra y modifica un bus', () => {
    const datos = { placa: 'PRUEBA-BUS', capacidadMaxima: 80, lineaId: 1, parqueoId: 1, estado: 'disponible' };
    cy.escribirApi('POST', '/api/buses', datos).then(({ body }) => {
      cy.escribirApi('PUT', `/api/buses/${body.id}`, datos).its('status').should('eq', 200);
    });
    cy.request('/api/buses').then(({ body }) => expect(JSON.stringify(body)).to.contain('PRUEBA-BUS'));
  });
});
