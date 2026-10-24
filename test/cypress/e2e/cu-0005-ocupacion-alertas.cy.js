describe('CU-0005 Registrar ocupación y gestionar alertas', () => {
  beforeEach(() => { cy.task('db:reset'); cy.ingresar(); cy.prepararLinea(); cy.iniciarRecorrido(); });

  it('CP-0005-01 registra una visita en el orden de la ruta', () => {
    cy.reload();
    cy.get('[data-route="operacion/visitas"]').click();
    cy.get('#visitas-content').contains('button', 'Nueva visita').click();
    cy.get('#visitas-form [name=recorridoId]').select('1');
    cy.get('#visitas-form [name=estacionId]').select('1');
    cy.get('#visitas-form [name=llegada]').invoke('val', '2026-10-01T06:00').trigger('change');
    cy.get('#visitas-form [name=salidaPrevista]').invoke('val', '2026-10-01T06:01').trigger('change');
    cy.get('#visitas-form [name=pasajerosAbordo]').type('40');
    cy.get('#visitas-form [name=pasajerosEsperando]').type('10');
    cy.get('#visitas-form').submit();
    cy.get('#visitas-list').should('contain', 'Estación A').and('contain', '40');
  });

  it('CP-0005-02 conserva la espera extra y registra la salida', () => {
    cy.registrarVisita(1, 10);
    cy.reload();
    cy.get('[data-route="operacion/visitas"]').click();
    cy.get('#visitas-list').contains('button', 'Registrar salida').click();
    cy.get('#accion-modal [name=salida]').type('2026-10-01T06:07');
    cy.get('#accion-modal form').submit();
    cy.request('/api/visitas').its('body.0').then(v => {
      expect(v.espera_extra_min).to.eq(5);
      expect(v.salida).not.to.eq(null);
    });
  });

  it('CP-0005-03 genera una alerta por ocupación', () => {
    cy.registrarVisita(1, 40, 160);
    cy.reload();
    cy.get('[data-route="operacion/alertas"]').click();
    cy.get('#alertas-list').should('contain', 'Pendiente').and('contain', 'Estación A');
  });

  it('CP-0005-04 asigna otro bus para atender la alerta', () => {
    cy.registrarVisita(1, 40, 160);
    cy.reload();
    cy.get('[data-route="operacion/alertas"]').click();
    cy.get('#alertas-list').contains('button', 'Asignar refuerzo').click();
    cy.get('#accion-modal [name=busId]').select('2');
    cy.get('#accion-modal form').submit();
    cy.get('#alertas-list').should('contain', 'Atendida').and('contain', 'BUS-B');
  });
});
