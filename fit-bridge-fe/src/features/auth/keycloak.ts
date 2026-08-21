import Keycloak from 'keycloak-js';

// Конфигурация Keycloak берётся из Vite environment.
// Значения по умолчанию оставлены только для локального запуска без .env-файла.
const keycloak = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL || 'http://localhost:8080',
  realm: import.meta.env.VITE_KEYCLOAK_REALM || 'fit-bridge',
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID || 'fit-bridge-web',
});

export default keycloak;
