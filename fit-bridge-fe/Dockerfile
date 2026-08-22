# Build the Vue application once and serve only immutable static assets at runtime.
FROM node:24-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . ./

# Safe fallbacks for an image started without runtime config.js.
ARG VITE_API_BASE_URL=/v2
ARG VITE_KEYCLOAK_URL=http://localhost:8080
ARG VITE_KEYCLOAK_REALM=fit-bridge
ARG VITE_KEYCLOAK_CLIENT_ID=fit-bridge-web
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL \
    VITE_KEYCLOAK_URL=$VITE_KEYCLOAK_URL \
    VITE_KEYCLOAK_REALM=$VITE_KEYCLOAK_REALM \
    VITE_KEYCLOAK_CLIENT_ID=$VITE_KEYCLOAK_CLIENT_ID

RUN npm run build

FROM nginx:stable-alpine

COPY deploy/nginx/default.conf /etc/nginx/conf.d/default.conf
COPY deploy/runtime-config.template.js /usr/share/nginx/html/config.template.js
COPY deploy/40-runtime-config.sh /docker-entrypoint.d/40-runtime-config.sh
COPY --from=build /app/dist/ /usr/share/nginx/html/

RUN sed -i 's/\r$//' /docker-entrypoint.d/40-runtime-config.sh \
    && chmod +x /docker-entrypoint.d/40-runtime-config.sh

EXPOSE 8080
