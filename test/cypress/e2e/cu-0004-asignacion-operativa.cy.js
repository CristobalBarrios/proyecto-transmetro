describe('CU-0004 Registrar asignación operativa', () => {
  beforeEach(() => { cy.task('db:reset'); cy.ingresar(); cy.prepararLinea(); });
  it('CP-0004-04 inicia un recorrido con bus y piloto', () => {
    cy.reload();
    cy.get('[data-route="operacion/recorridos"]').click();
    cy.get('#recorridos-content').contains('button', 'Nuevo recorrido').click();
    cy.get('#recorridos-form [name=lineaId]').select('1');
    cy.get('#recorridos-form [name=busId]').select('1');
    cy.get('#recorridos-form [name=pilotoId]').select('1');
    cy.get('#recorridos-form').submit();
    cy.get('#recorridos-list').should('contain', 'BUS-A').and('contain', 'Piloto de prueba');
  });
  it('CP-0004-05 finaliza un recorrido con todas sus visitas', () => {
    cy.iniciarRecorrido();
    [1, 2].forEach(id => {
      cy.registrarVisita(id);
      cy.escribirApi('PUT', `/api/visitas/${id}/salida`, { salida: '2026-10-01T12:02:00Z' });
    });
    cy.reload();
    cy.get('[data-route="operacion/recorridos"]').click();
    cy.get('#recorridos-list').contains('button', 'Finalizar').click();
    cy.request('/api/recorridos').its('body.0.fin').should('not.be.null');
    cy.get('#recorridos-list').should('not.contain', 'Finalizar');
  });
});
