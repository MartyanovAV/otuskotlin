import Keycloak from 'keycloak-js';

// Конфигурация Keycloak
// Мы используем Envoy Gateway на порту 8080, который проксирует запросы к Keycloak
const keycloak = new Keycloak({
  url: 'http://localhost:8080',
  realm: 'fit-bridge',
  clientId: 'fit-bridge-service',
});

export default keycloak;
