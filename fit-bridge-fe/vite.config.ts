import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import tailwindcss from '@tailwindcss/vite'
import { VitePWA } from 'vite-plugin-pwa'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
    tailwindcss(),
    VitePWA({
      registerType: 'autoUpdate',
      workbox: {
        // config.js is generated when a container starts. It must be read
        // from the network instead of being frozen in the precache manifest.
        globIgnores: ['**/config.js'],
        // Envoy serves Keycloak on the same origin. A navigation fallback for
        // these paths would return the SPA shell instead of Keycloak's OIDC
        // endpoint, causing redirect_uri recursion after the SW is installed.
        navigateFallbackDenylist: [/^\/(?:realms|admin|resources)(?:\/|$)/],
      },
      manifest: {
        name: 'FitBridge',
        short_name: 'FitBridge',
        theme_color: '#0d0e11',
        background_color: '#0d0e11',
        display: 'standalone',
        icons: [
          {
            src: '/favicon.ico',
            sizes: '32x32',
            type: 'image/x-icon'
          }
        ]
      }
    })
  ],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/v2'),
      },
    },
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
})
