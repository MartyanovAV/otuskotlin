import { defineStore } from 'pinia';
import { ref } from 'vue';
import keycloak from './keycloak';
import type { KeycloakProfile } from 'keycloak-js';
import { getFitBridgeRedirectUri } from '@/shared/config/runtime';

export const useAuthStore = defineStore('auth', () => {
  const isAuthenticated = ref(false);
  const isInitialized = ref(false);
  const initializationError = ref<string | null>(null);
  const userProfile = ref<KeycloakProfile | null>(null);
  // Токен хранить в store не нужно — он всегда актуален через keycloak.token
  // (keycloak-js сам обновляет его через onTokenExpired)
  const roles = ref<string[]>([]);

  const syncRoles = () => {
    roles.value = keycloak.realmAccess?.roles ?? [];
  };

  const initKeycloak = async () => {
    initializationError.value = null;
    try {
      const authenticated = await keycloak.init({
        onLoad: 'login-required',
        pkceMethod: 'S256',
        redirectUri: getFitBridgeRedirectUri(),
        // Отключаем iframe SSO-проверку: не нужна для SPA без поддоменов
        checkLoginIframe: false,
      });

      isAuthenticated.value = authenticated;
      isInitialized.value = true;

      if (authenticated) {
        try {
          userProfile.value = await keycloak.loadUserProfile();
        } catch {
          userProfile.value = {
            username: keycloak.tokenParsed?.preferred_username,
            firstName: (keycloak.idTokenParsed?.given_name ?? keycloak.tokenParsed?.given_name) as string | undefined,
            lastName: (keycloak.idTokenParsed?.family_name ?? keycloak.tokenParsed?.family_name) as string | undefined,
            email: (keycloak.idTokenParsed?.email ?? keycloak.tokenParsed?.email) as string | undefined,
          };
        }

        syncRoles();

        // Автоматическое обновление токена за 30 секунд до истечения
        keycloak.onTokenExpired = async () => {
          try {
            await keycloak.updateToken(30);
          } catch (error) {
            console.error('Failed to refresh token, logging out', error);
            logout();
          }
        };

        keycloak.onAuthRefreshSuccess = syncRoles;
      }
    } catch (error) {
      console.error('Keycloak initialization failed', error);
      isAuthenticated.value = false;
      isInitialized.value = true;
      initializationError.value = 'Ошибка авторизации, попробуйте позже';
    }
  };

  const login = () => {
    return keycloak.login({ redirectUri: getFitBridgeRedirectUri() });
  };

  const logout = () => {
    return keycloak.logout({ redirectUri: getFitBridgeRedirectUri() });
  };

  const hasRole = (role: string) => roles.value.includes(role);

  return {
    isAuthenticated,
    isInitialized,
    initializationError,
    userProfile,
    roles,
    initKeycloak,
    login,
    logout,
    hasRole,
  };
});
