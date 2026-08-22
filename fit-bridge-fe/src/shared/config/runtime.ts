export interface FitBridgeRuntimeConfig {
  apiBaseUrl?: string
  keycloakUrl?: string
  keycloakRealm?: string
  keycloakClientId?: string
}

declare global {
  interface Window {
    __FITBRIDGE_CONFIG__?: FitBridgeRuntimeConfig
  }
}

const runtimeConfig =
  typeof window === 'undefined' ? undefined : window.__FITBRIDGE_CONFIG__

const fromRuntimeOrBuild = (runtimeValue: string | undefined, buildValue: string | undefined) =>
  runtimeValue?.trim() || buildValue?.trim()

export const fitBridgeConfig = {
  apiBaseUrl: fromRuntimeOrBuild(runtimeConfig?.apiBaseUrl, import.meta.env.VITE_API_BASE_URL) ?? '/api',
  keycloakUrl: fromRuntimeOrBuild(runtimeConfig?.keycloakUrl, import.meta.env.VITE_KEYCLOAK_URL) ?? 'http://localhost:8080',
  keycloakRealm: fromRuntimeOrBuild(runtimeConfig?.keycloakRealm, import.meta.env.VITE_KEYCLOAK_REALM) ?? 'fit-bridge',
  keycloakClientId:
    fromRuntimeOrBuild(runtimeConfig?.keycloakClientId, import.meta.env.VITE_KEYCLOAK_CLIENT_ID) ??
    'fit-bridge-web',
}

const keycloakPathPrefixes = ['/realms/', '/admin/', '/resources/']
const oauthCallbackParameters = ['code', 'state', 'session_state', 'iss', 'error', 'error_description', 'error_uri']

const hasOAuthCallbackParameter = (parameters: URLSearchParams) =>
  oauthCallbackParameters.some((parameter) => parameters.has(parameter))

// Preserve a requested SPA route across login, but never use a Keycloak endpoint
// or an OAuth callback payload as the next redirect URI. The latter caused a
// redirect_uri recursion when the service worker accidentally served the SPA on
// /realms/*.
export const getFitBridgeRedirectUri = () => {
  const currentUrl = new URL(window.location.href)
  const isKeycloakPath = keycloakPathPrefixes.some((prefix) => currentUrl.pathname.startsWith(prefix))

  if (currentUrl.origin !== window.location.origin || isKeycloakPath) {
    return new URL(import.meta.env.BASE_URL, window.location.origin).toString()
  }

  currentUrl.hash = ''
  oauthCallbackParameters.forEach((parameter) => currentUrl.searchParams.delete(parameter))
  return currentUrl.toString()
}

// keycloak-js consumes the authorization response during init, but with the
// fragment response mode its callback parameters can remain in the visible URL.
// Remove them after the router has completed its initial navigation, while
// retaining the requested SPA route and any application query parameters.
export const clearFitBridgeOAuthCallback = () => {
  const currentUrl = new URL(window.location.href)
  const fragmentParameters = new URLSearchParams(currentUrl.hash.slice(1))
  const hasQueryCallback = hasOAuthCallbackParameter(currentUrl.searchParams)
  const hasFragmentCallback = hasOAuthCallbackParameter(fragmentParameters)

  if (!hasQueryCallback && !hasFragmentCallback) {
    return
  }

  oauthCallbackParameters.forEach((parameter) => currentUrl.searchParams.delete(parameter))

  if (hasFragmentCallback) {
    currentUrl.hash = ''
  }

  window.history.replaceState(window.history.state, '', `${currentUrl.pathname}${currentUrl.search}${currentUrl.hash}`)
}
