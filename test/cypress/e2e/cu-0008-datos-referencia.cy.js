describe('CU-0008 Actualizar datos de referencia', () => {
  beforeEach(() => { cy.task('db:reset'); cy.ingresar(); });
  it('CP-0008-02 carga una ruta y sus estaciones desde una fuente autorizada', () => {
    const datos = { rutas: [{ municipalidadId: 1, codigo: 'REF-1', nombre: 'Línea de referencia', estaciones: [
      { nombre: 'Referencia A', direccion: 'Dirección A', zona: 1, codigoRuta: 'REF-A', distanciaKm: 0 },
      { nombre: 'Referencia B', direccion: 'Dirección B', zona: 2, codigoRuta: 'REF-B', distanciaKm: 1.5 }
    ] }] };
    cy.get('[data-route="red/referencias"]').click();
    cy.get('#referencias-form [name=fuente]').type('Archivo municipal autorizado');
    cy.get('#referencias-form [name=archivo]').selectFile({ contents: Cypress.Buffer.from(JSON.stringify(datos)), fileName: 'referencias.json', mimeType: 'application/json' });
    cy.get('#referencias-form [name=autorizada]').check();
    cy.get('#referencias-form').submit();
    cy.get('#referencias-resumen').should('contain', 'Rutas aceptadas: 1. Rechazadas: 0.');
    cy.request('/api/lineas').its('body').should('deep.include', { id: 2, municipalidadId: 1, codigo: 'REF-1', nombre: 'Línea de referencia', estado: 'borrador' });
  });

  it('CP-0008-01 registra y modifica una municipalidad', () => {
    const datos = { nombre: 'Municipalidad nueva' };
    cy.escribirApi('POST', '/api/municipalidades', datos).then(({ body }) => {
      cy.escribirApi('PUT', `/api/municipalidades/${body.id}`, datos).its('status').should('eq', 200);
    });
    cy.request('/api/municipalidades').then(({ body }) => expect(JSON.stringify(body)).to.contain('Municipalidad nueva'));
  });
});
