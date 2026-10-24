describe('CU-0006 Consultar reportes operativos', () => {
  beforeEach(() => { cy.task('db:reset'); cy.ingresar(); cy.prepararLinea(); });

  it('CP-0006-01 consulta los reportes de la red y la flota', () => {
    cy.reload();
    cy.get('[data-route="reportes"]').click();
    cy.get('#reportes-form [name=tipo]').select('lineas');
    cy.get('#reportes-form').submit();
    cy.get('#reportes-list').should('contain', 'PRUEBA-1').and('contain', '1.250');
    cy.get('#reportes-form [name=tipo]').select('estaciones');
    cy.get('#reportes-form').submit();
    cy.get('#reportes-list').should('contain', 'Estación A');
    cy.get('#reportes-form [name=tipo]').select('buses');
    cy.get('#reportes-form').submit();
    cy.get('#reportes-list').should('contain', 'BUS-A');
  });

  it('CP-0006-02 filtra los reportes operativos por período y línea', () => {
    cy.escribirApi('POST', '/api/eventos', { tipo: 'Prueba', descripcion: 'Evento de reporte', lineaId: 1 });
    cy.reload();
    cy.get('[data-route="reportes"]').click();
    cy.get('#reportes-form [name=tipo]').select('eventos');
    cy.get('#reportes-form [name=desde]').type('2026-01-01');
    cy.get('#reportes-form [name=hasta]').type('2026-12-31');
    cy.get('#reportes-form [name=lineaId]').select('1');
    cy.get('#reportes-form').submit();
    cy.get('#reportes-list').should('contain', 'Evento de reporte').and('contain', 'Total de registros: 1');
  });
});
