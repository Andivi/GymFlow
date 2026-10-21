# Build context: repository root (see infra/compose.yaml).
FROM node:22-alpine AS build
WORKDIR /workspace
COPY apps/web/package.json apps/web/package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY apps/web ./
RUN npm run build

FROM nginx:1.28-alpine
COPY infra/docker/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/dist /usr/share/nginx/html
EXPOSE 80
