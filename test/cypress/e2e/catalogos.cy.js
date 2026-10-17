describe('Catálogos', () => {
  beforeEach(() => { cy.task('db:reset'); cy.ingresar(); });
  it('CAT-01 crea, modifica y conserva una línea', () => {
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
  it('CAT-02 rechaza un código de línea duplicado', () => {
    cy.escribirApi('POST', '/api/lineas', { municipalidadId: 1, codigo: 'PRUEBA-1', nombre: 'Duplicado' }, false)
      .its('status').should('eq', 409);
    cy.request('/api/lineas').its('body').should('have.length', 1);
  });
  it('CAT-03 crea una estación con todos sus campos', () => {
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
  it('CAT-04 guarda los demás catálogos y permite consultar los registros', () => {
    const casos = [
      ['municipalidades', { nombre: 'Municipalidad nueva' }, 'Municipalidad nueva'],
      ['parqueos', { nombre: 'Parqueo nuevo', estacionId: 1 }, 'Parqueo nuevo'],
      ['buses', { placa: 'PRUEBA-BUS', capacidadMaxima: 80, lineaId: 1, parqueoId: 1, estado: 'disponible' }, 'PRUEBA-BUS'],
      ['pilotos', { nombre: 'Piloto nuevo', residencia: 'Residencia', telefono: '12345678', correo: null }, 'Piloto nuevo'],
      ['guardias', { nombre: 'Guardia nuevo', telefono: '12345678' }, 'Guardia nuevo'],
      ['accesos', { estacionId: 2, codigo: 'ACC-B', descripcion: 'Acceso B' }, 'ACC-B']
    ];
    casos.forEach(([key, payload, texto]) => {
      cy.escribirApi('POST', `/api/${key}`, payload).then(({ body }) => {
        cy.escribirApi('PUT', `/api/${key}/${body.id}`, payload).its('status').should('eq', 200);
      });
      cy.request(`/api/${key}`).then(({ body }) => expect(JSON.stringify(body)).to.contain(texto));
    });
  });
});
