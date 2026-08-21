# fit-bridge-fe

This template should help get you started developing with Vue 3 in Vite.

## FitBridge configuration

The frontend uses the public `fit-bridge-web` Keycloak client with Authorization Code + PKCE.
Local defaults are stored in `.env`; production values must be supplied by the deployment and
must match the redirect URI configured in Keycloak:

```dotenv
VITE_API_BASE_URL=/api
VITE_KEYCLOAK_URL=http://localhost:8080
VITE_KEYCLOAK_REALM=fit-bridge
VITE_KEYCLOAK_CLIENT_ID=fit-bridge-web
```

Run the local infrastructure from `deploy/` before opening the application. The direct grant
client `fit-bridge-smoke` is reserved for local smoke checks and is not used by the browser UI.

## Recommended IDE Setup

[VS Code](https://code.visualstudio.com/) + [Vue (Official)](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (and disable Vetur).

## Recommended Browser Setup

- Chromium-based browsers (Chrome, Edge, Brave, etc.):
  - [Vue.js devtools](https://chromewebstore.google.com/detail/vuejs-devtools/nhdogjmejiglipccpnnnanhbledajbpd)
  - [Turn on Custom Object Formatter in Chrome DevTools](http://bit.ly/object-formatters)
- Firefox:
  - [Vue.js devtools](https://addons.mozilla.org/en-US/firefox/addon/vue-js-devtools/)
  - [Turn on Custom Object Formatter in Firefox DevTools](https://fxdx.dev/firefox-devtools-custom-object-formatters/)

## Type Support for `.vue` Imports in TS

TypeScript cannot handle type information for `.vue` imports by default, so we replace the `tsc` CLI with `vue-tsc` for type checking. In editors, we need [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) to make the TypeScript language service aware of `.vue` types.

## Customize configuration

See [Vite Configuration Reference](https://vite.dev/config/).

## Project Setup

```sh
npm install
```

### Compile and Hot-Reload for Development

```sh
npm run dev
```

### Type-Check, Compile and Minify for Production

```sh
npm run build
```

### Run Unit Tests with [Vitest](https://vitest.dev/)

```sh
npm run test:unit
```

### Run End-to-End Tests with [Playwright](https://playwright.dev)

```sh
# Install browsers for the first run
npx playwright install

# When testing on CI, must build the project first
npm run build

# Runs the end-to-end tests
npm run test:e2e
# Runs the tests only on Chromium
npm run test:e2e -- --project=chromium
# Runs the tests of a specific file
npm run test:e2e -- tests/example.spec.ts
# Runs the tests in debug mode
npm run test:e2e -- --debug
```

Playwright tests require local Keycloak, Envoy and Training Service to be running. Override the
test user or frontend origin with `E2E_AUTH_USERNAME`, `E2E_AUTH_PASSWORD` and `E2E_BASE_URL`.

### Lint with [ESLint](https://eslint.org/)

```sh
npm run lint
```
