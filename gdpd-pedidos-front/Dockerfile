# Stage 1: Build Angular application
FROM node:16-alpine AS builder

WORKDIR /build

# Copy package.json and package-lock.json
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy source code
COPY . .

# Build production bundle
RUN npm run build

# Stage 2: Serve with nginx
FROM nginx:alpine

WORKDIR /usr/share/nginx/html

LABEL maintainer="GDPD Release Team"
LABEL version="1.0.0"
LABEL description="GDPD Pedidos Frontend - Angular 12 + Thin3 thin client"

# Copy built application from builder stage
COPY --from=builder /build/dist/gdpd-pedidos-front .

# Copy nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Expose port
EXPOSE 4200

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:4200/health || exit 1

# Run nginx
CMD ["nginx", "-g", "daemon off;"]
