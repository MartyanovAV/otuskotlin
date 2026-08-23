import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { clearFitBridgeOAuthCallback, fitBridgeConfig, getFitBridgeRedirectUri } from '../runtime'

describe('fitBridgeConfig', () => {
  beforeEach(() => {
    delete window.__FITBRIDGE_CONFIG__
  })

  afterEach(() => {
    delete window.__FITBRIDGE_CONFIG__
  })

  it('reads values from window.__FITBRIDGE_CONFIG__ when available', () => {
    window.__FITBRIDGE_CONFIG__ = {
      apiBaseUrl: '/custom-api',
      keycloakUrl: 'https://79-133-181-52.sslip.io',
      keycloakRealm: 'custom-realm',
      keycloakClientId: 'custom-client',
    }

    expect(fitBridgeConfig.apiBaseUrl).toBe('/custom-api')
    expect(fitBridgeConfig.keycloakUrl).toBe('https://79-133-181-52.sslip.io')
    expect(fitBridgeConfig.keycloakRealm).toBe('custom-realm')
    expect(fitBridgeConfig.keycloakClientId).toBe('custom-client')
  })

  it('falls back to window.location.origin when keycloakUrl is missing from both runtime and build env', () => {
    vi.stubEnv('VITE_KEYCLOAK_URL', '')
    window.__FITBRIDGE_CONFIG__ = {
      keycloakUrl: '',
    }

    expect(fitBridgeConfig.keycloakUrl).toBe(window.location.origin)
    vi.unstubAllEnvs()
  })
})

describe('getFitBridgeRedirectUri', () => {
  afterEach(() => {
    window.history.replaceState({}, '', '/')
  })

  it('returns sanitized redirect URI without OAuth callback params', () => {
    window.history.replaceState({}, '', '/plans?state=abc&code=123')
    expect(getFitBridgeRedirectUri()).toBe(`${window.location.origin}/plans`)
  })
})

describe('clearFitBridgeOAuthCallback', () => {
  afterEach(() => {
    window.history.replaceState({}, '', '/')
  })

  it('removes the fragment callback while preserving the requested SPA route', () => {
    window.history.replaceState(
      {},
      '',
      '/clients?filter=active#state=state-value&session_state=session-value&iss=https%3A%2F%2Fidp.example&code=code-value',
    )

    clearFitBridgeOAuthCallback()

    expect(window.location.pathname).toBe('/clients')
    expect(window.location.search).toBe('?filter=active')
    expect(window.location.hash).toBe('')
  })

  it('removes callback parameters returned in the query without changing an application fragment', () => {
    window.history.replaceState({}, '', '/plans?code=code-value&state=state-value&view=calendar#details')

    clearFitBridgeOAuthCallback()

    expect(window.location.pathname).toBe('/plans')
    expect(window.location.search).toBe('?view=calendar')
    expect(window.location.hash).toBe('#details')
  })

  it('does not modify an URL without OAuth callback parameters', () => {
    window.history.replaceState({}, '', '/plans?view=calendar#details')

    clearFitBridgeOAuthCallback()

    expect(window.location.pathname).toBe('/plans')
    expect(window.location.search).toBe('?view=calendar')
    expect(window.location.hash).toBe('#details')
  })
})
