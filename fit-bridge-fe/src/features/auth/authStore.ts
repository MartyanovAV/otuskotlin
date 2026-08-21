import { defineStore } from 'pinia';
import { ref } from 'vue';
import keycloak from './keycloak';
import type { KeycloakProfile } from 'keycloak-js';

export const useAuthStore = defineStore('auth', () => {
  const isAuthenticated = ref(false);
  const userProfile = ref<KeycloakProfile | null>(null);
  // Токен хранить в store не нужно — он всегда актуален через keycloak.token
  // (keycloak-js сам обновляет его через onTokenExpired)
  const roles = ref<string[]>([]);

  const initKeycloak = async () => {
    try {
      const authenticated = await keycloak.init({
        onLoad: 'login-required',
        pkceMethod: 'S256',
        // Отключаем iframe SSO-проверку: не нужна для SPA без поддоменов
        checkLoginIframe: false,
      });

      isAuthenticated.value = authenticated;

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

        if (keycloak.realmAccess?.roles) {
          roles.value = keycloak.realmAccess.roles;
        }

        // Автоматическое обновление токена за 30 секунд до истечения
        keycloak.onTokenExpired = async () => {
          try {
            await keycloak.updateToken(30);
          } catch (error) {
            console.error('Failed to refresh token, logging out', error);
            logout();
          }
        };
      }
    } catch (error) {
      console.error('Keycloak initialization failed', error);
    }
  };

  const login = () => {
    keycloak.login();
  };

  const logout = () => {
    keycloak.logout();
  };

  const hasRole = (role: string) => roles.value.includes(role);

  return {
    isAuthenticated,
    userProfile,
    roles,
    initKeycloak,
    login,
    logout,
    hasRole,
  };
});
