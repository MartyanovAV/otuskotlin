FROM nginx:stable-alpine

COPY volumes/nginx/default.conf /etc/nginx/conf.d/
COPY volumes/nginx/nginx.conf /etc/nginx/
COPY volumes/nginx/html /usr/share/nginx/html

EXPOSE 8080/tcp

ENTRYPOINT ["nginx", "-g", "daemon off;"]
