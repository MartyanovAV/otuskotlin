import Keycloak from 'keycloak-js';
import { fitBridgeConfig } from '@/shared/config/runtime';

// Docker deployment supplies config.js at container startup; Vite env remains a dev fallback.
const keycloak = new Keycloak({
  url: fitBridgeConfig.keycloakUrl,
  realm: fitBridgeConfig.keycloakRealm,
  clientId: fitBridgeConfig.keycloakClientId,
});

export default keycloak;
