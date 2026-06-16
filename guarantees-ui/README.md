# Guarantees UI - Angular 17 Standalone

Frontend application for the guarantees service built with Angular 17 standalone API and Material Design.

## Project Structure

```
guarantees-ui/
├── src/
│   ├── app/
│   │   ├── features/
│   │   │   └── guarantees/
│   │   │       └── guarantees.component.ts
│   │   ├── app.component.ts          # Root component with router-outlet
│   │   ├── app.config.ts             # Standalone app configuration
│   │   └── app.routes.ts             # Route definitions
│   ├── main.ts                       # Bootstrap entry point
│   ├── styles.scss                   # Global styles with Material theme
│   ├── index.html                    # HTML template
│   └── assets/                       # Static assets
├── angular.json                      # Angular CLI configuration
├── tsconfig.json                     # TypeScript configuration (strict: true)
├── tsconfig.app.json                 # App-specific TypeScript config
├── proxy.conf.json                   # Dev server proxy configuration
├── package.json                      # Dependencies
├── nginx.conf                        # Production nginx configuration
├── Dockerfile                        # Multi-stage Docker build
├── .gitignore                        # Git ignore rules
└── README.md                         # This file
```

## Setup

### Prerequisites
- Node.js 18+
- npm 9+
- Angular CLI 17

### Installation

```bash
npm ci
```

### Development

Start the dev server with API proxy:
```bash
npm start
```

The app runs on `http://localhost:4200` and proxies `/api/*` requests to `http://localhost:8080`.

### Build

Production build:
```bash
npm run build
```

Output: `dist/guarantees-ui/`

## Docker

### Build Image
```bash
docker build -t guarantees-ui:latest .
```

### Run Container
```bash
docker run -p 80:80 guarantees-ui:latest
```

The container serves the app on port 80 and proxies API requests to `http://backend:8080` (Docker service name).

## Key Features

- **Standalone Components**: Uses Angular 17 standalone API (no NgModules)
- **Material Design**: Integrated Material Design 17 with indigo-pink theme
- **Routing**: Client-side routing with lazy loading support
- **HTTP Client**: Configured for backend integration
- **Animations**: Material animations enabled
- **Docker**: Multi-stage production build optimized for nginx

## Critical Note (E07)

The `nginx.conf` uses the Docker service name `http://backend:8080` for API proxying, not localhost. This allows proper inter-service communication in Docker Compose deployments.

## Development Guidelines

- All components are standalone (no modules)
- Use strict TypeScript mode (`strict: true`)
- Material components imported directly where needed
- Routing via `app.routes.ts`
- Global styles in `src/styles.scss`
