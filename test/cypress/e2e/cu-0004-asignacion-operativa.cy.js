describe('CU-0004 Registrar asignación operativa', () => {
  beforeEach(() => { cy.task('db:reset'); cy.ingresar(); cy.prepararLinea(); });
  it('CP-0004-04 inicia un recorrido con bus y piloto', () => {
    cy.escribirApi('PUT', '/api/operacion/lineas/1/estado', { estado: 'operativa' });
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
  it('CP-0004-06 registra y cierra un evento operativo', () => {
    cy.get('[data-route="operacion/eventos"]').click();
    cy.get('#eventos-content').contains('button', 'Nuevo evento').click();
    cy.get('#eventos-form [name=tipo]').type('Incidencia');
    cy.get('#eventos-form [name=descripcion]').type('Revisión de acceso');
    cy.get('#eventos-form [name=estacionId]').select('1');
    cy.get('#eventos-form').submit();
    cy.get('#eventos-list').contains('button', 'Atender').click();
    cy.get('#eventos-list').should('contain', 'en_atencion');
    cy.get('#eventos-list').contains('button', 'Cerrar').click();
    cy.get('#eventos-list').should('contain', 'cerrado');
  });
  it('CP-0004-01 activa una línea preparada', () => {
    cy.reload();
    cy.get('[data-route="operacion"]').click();
    cy.get('#operacion-list').contains('tr', 'PRUEBA-1').should('contain', 'Lista para operar').contains('button', 'Activar').click();
    cy.get('#operacion-list').should('contain', 'operativa');
    cy.reload();
    cy.get('#operacion-list').contains('button', 'Desactivar').should('exist');
    cy.request('/api/lineas').its('body.0.estado').should('eq', 'operativa');
  });
  it('CP-0004-02 desactiva una línea sin recorridos', () => {
    cy.escribirApi('PUT', '/api/operacion/lineas/1/estado', { estado: 'operativa' });
    cy.reload();
    cy.get('[data-route="operacion"]').click();
    cy.get('#operacion-list').contains('button', 'Desactivar').click();
    cy.get('#operacion-list').should('contain', 'inactiva');
    cy.request('/api/lineas').its('body.0.estado').should('eq', 'inactiva');
  });
  it('CP-0004-03 conserva una línea operativa al modificar un bus sin alterar su preparación', () => {
    cy.escribirApi('PUT', '/api/operacion/lineas/1/estado', { estado: 'operativa' });
    cy.escribirApi('PUT', '/api/buses/1', { placa: 'BUS-A', capacidadMaxima: 90, lineaId: 1, parqueoId: 1, estado: 'disponible' });
    cy.request('/api/operacion/lineas').its('body.0').then(linea => {
      expect(linea.estado).to.equal('operativa');
      expect(linea.pendientes).to.deep.equal([]);
    });
  });
});
