# prepare-apis-generated.js — Análisis completo

> Fuente: `containers/nova-org/toolchain/prepare-apis-generated.js` (190 líneas, v2.0.1)

## Propósito

Script Node.js que procesa las librerías Angular generadas por `nova generate-api-code` para que
puedan ser importadas en el proyecto Angular. **Sin este script, Angular NO puede resolver los
imports de las librerías generadas aunque los ficheros existan en disco.**

## Versión y changelog

```
V 2.0.1 - return to original folder on removeFolderOnDependency cuando la carpeta no existe
V 2.0.1 - añade asyncapi-generated/backToFront/serverpush/client a API_GENERATED_PATHS
V 1.0.0 - versión inicial para generate-api-core
```

## Flujo de ejecución real

### 1. Búsqueda de carpetas con código generado

```javascript
const API_GENERATED_PATHS = [
  'api-generated',                                    // APIs REST generadas
  'asyncapi-generated/backToFront/serverpush/client'  // AsyncAPI ServerPush client
];
```

El script busca estas carpetas relativas al directorio actual. Si ninguna existe, termina con
`"No hay código de API generado"`.

### 2. Dependencia base: generator-lib-commons-angular

Antes de procesar las librerías generadas, instala `generator-lib-commons-angular@^3.0.0`
(desde Artifactory NOVA). Esta es una dependencia compartida requerida por todas las librerías.

### 3. Para cada librería generada (subcarpeta en api-generated/):

```
Para cada librería ≠ artifactory:
  a) cd <librería>
  b) npm install            → Instala dependencias de la librería
  c) ng build               → Compila la librería Angular
  d) cd <proyecto>
  e) npm install --save <librería>/lib-generated/dist  → Instala el resultado compilado
  f) Limpia node_modules/ y .angular/ de la librería (workspace cleanup)
```

### 4. Resultado final

Las librerías quedan instaladas como dependencias locales del proyecto Angular,
con sus dist/ compilados disponibles para importación:

```typescript
import { MiApiService } from '<nombre-librería>';
```

## Diagrama de flujo

```
nova generate-api-code
    ↓
api-generated/
├── servicio-cuentas/         ← subcarpeta generada
│   ├── package.json
│   ├── src/                  ← código TypeScript generado
│   └── lib-generated/
│       └── dist/             ← resultado de ng build
└── servicio-notificaciones/
    └── ...

    ↓ node prepare-apis-generated.js

1. npm install generator-lib-commons-angular@^3.0.0
2. Para cada subcarpeta:
   - cd servicio-cuentas && npm install && ng build
   - npm install --save api-generated/servicio-cuentas/lib-generated/dist
   - rm -rf servicio-cuentas/node_modules servicio-cuentas/.angular
```

## Constantes clave

```javascript
const LIB_COMMONS = 'generator-lib-commons-angular@^3.0.0';
const DIST_PATH = '/lib-generated/dist';
const NODE_MODULES = '/node_modules';
const CACHE_ANGULAR = '/.angular';
```

## Errores comunes

| Síntoma | Causa | Solución |
|---------|-------|----------|
| `No hay código de API generado` | No existe carpeta `api-generated/` | Ejecutar `nova generate-api-code` primero |
| Error en `npm install` dentro de librería | Falta acceso a Artifactory NOVA | Verificar `.npmrc` con registry correcto |
| Error en `ng build` | Dependencias incompatibles o Angular CLI no disponible | Verificar versión Angular CLI global |
| Import no resuelto en Angular | No se ejecutó prepare-apis-generated.js | Ejecutar el script |

## Integración con AsyncAPI

Desde v2.0.1, el script también procesa librerías generadas por AsyncAPI en la ruta
`asyncapi-generated/backToFront/serverpush/client`, que son clientes Angular para
comunicación ServerPush (WebSocket/SSE) generados desde especificaciones AsyncAPI.
