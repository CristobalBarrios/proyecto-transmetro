Cypress.Commands.add('ingresar', () => {
  cy.visit('/login');
  cy.get('#csrf-token').should('not.have.value', '');
  cy.env(['username', 'password'], { log: false }).then(({ username, password }) => {
    cy.get('[name=username]').type(username, { log: false });
    cy.get('[name=password]').type(password, { log: false });
  });
  cy.get('#login-form').submit();
  cy.location('pathname').should('eq', '/');
  cy.get('#summary-cards .summary-card').should('have.length', 9);
});
Cypress.Commands.add('escribirApi', (method, url, body, failOnStatusCode = true) => {
  cy.request('/api/csrf').then(({ body: token }) => cy.request({
    method, url, body, headers: { [token.encabezado]: token.valor }, failOnStatusCode
  }));
});
