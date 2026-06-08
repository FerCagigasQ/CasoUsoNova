FROM node:16-alpine as builder
WORKDIR /app
ARG API_URL=http://localhost:8080
ARG ENV=int
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build -- --configuration=${ENV} || npm run build

FROM nginx:1.21-alpine
COPY --from=builder /app/dist/gdpd-pedidos-front /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
