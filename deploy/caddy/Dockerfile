# syntax=docker/dockerfile:1.7
#
# FitBridge Caddy edge image.
#
# Caddy 2.11.4 is pinned (current upstream stable). The caddy-jwt plugin
# (ggicci/caddy-jwt v1.1.0) is compiled in via xcaddy so that the resulting
# binary speaks ACMEv2 and validates RS256 JWTs against a JWKS URL out of
# the box. The image bakes both the local and the prod Caddyfile templates
# so the entrypoint can switch between them via the mounted /etc/caddy/Caddyfile.
#
# Bump Caddy and the plugin in a separate PR; verify the caddy-jwt release
# notes for any breaking changes to the jwtauth directive.

FROM caddy:2.11.4-builder-alpine AS builder
RUN --mount=type=cache,target=/go/pkg/mod \
    --mount=type=cache,target=/root/.cache/go-build \
    xcaddy build \
    --with github.com/ggicci/caddy-jwt@v1.1.0 \
    --output /usr/bin/caddy

FROM caddy:2.11.4-alpine
COPY --from=builder /usr/bin/caddy /usr/bin/caddy
COPY Caddyfile.local /etc/caddy/Caddyfile
COPY Caddyfile.prod.template /etc/caddy/Caddyfile.prod.template
EXPOSE 80 443
# Use a state directory on a persistent volume so that ACME account keys
# and issued certificates survive container restarts.
VOLUME ["/data/caddy", "/config/caddy"]
ENV XDG_DATA_HOME=/data/caddy \
    XDG_CONFIG_HOME=/config/caddy
CMD ["caddy", "run", "--config", "/etc/caddy/Caddyfile", "--adapter", "caddyfile"]
