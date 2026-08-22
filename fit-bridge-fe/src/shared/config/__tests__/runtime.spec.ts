import { afterEach, describe, expect, it } from 'vitest'
import { clearFitBridgeOAuthCallback } from '../runtime'

afterEach(() => {
  window.history.replaceState({}, '', '/')
})

describe('clearFitBridgeOAuthCallback', () => {
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
