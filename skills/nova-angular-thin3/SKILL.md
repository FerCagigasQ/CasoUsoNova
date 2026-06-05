---
name: Angular & Thin3 Framework
slug: nova-angular-thin3
description: Desarrollo frontend con Angular 12+ y el framework Thin3 de BBVA — estructura de proyecto, componentes, servicios, routing, guards, interceptors, y patrones de integración.
---

# Angular & Thin3 — NOVA

## Stack Frontend NOVA

| Tecnología | Versión | Propósito |
|-----------|---------|-----------|
| Angular | 12+ | Framework SPA principal |
| Thin3 | Latest | Capa corporativa BBVA sobre Angular |
| TypeScript | 4.x | Lenguaje tipado |
| RxJS | 7.x | Programación reactiva |
| SCSS | — | Estilos con variables corporativas |
| Karma + Jasmine | — | Testing unitario |
| ESLint + Prettier | — | Linting y formateo |

## Angular CLI Commands

```bash
# Proyecto
ng new mi-frontal --routing --style=scss
ng serve --port 4200
ng build --configuration=production

# Generación
ng generate component features/dashboard/components/resumen    # ng g c
ng generate service core/services/auth                         # ng g s
ng generate module features/clientes --routing                 # ng g m
ng generate guard core/guards/auth                            # ng g g
ng generate interceptor core/interceptors/auth                # ng g interceptor
ng generate pipe shared/pipes/fecha-formato                   # ng g p

# Testing
ng test                                    # Unit tests (Karma)
ng test --code-coverage                    # Con cobertura
ng e2e                                     # E2E tests
ng lint                                    # ESLint
```

## Integración con NOVA CLI (generación de código)

```bash
# Flujo completo para integrar APIs backend en el frontal:
# 1. Generar código cliente TypeScript desde Swagger spec
nova generate-api-code

# 2. OBLIGATORIO: Compilar e instalar librerías Angular generadas
node prepare-apis-generated.js
# → Instala generator-lib-commons-angular@^3.0.0 (dependencia base)
# → Para cada librería en api-generated/:
#     npm install → ng build → npm install --save <lib>/lib-generated/dist
# → También procesa asyncapi-generated/backToFront/serverpush/client
# → Limpia node_modules/ y .angular/ de cada librería tras compilar
# → Sin este paso, Angular NO puede resolver los imports de librerías generadas
```

### Resultado de `nova generate-api-code` para Angular

```
api-generated/                           # Directorio de salida (API_GENERATION_FOLDER)
├── servicio-cuentas/                    # Una librería Angular por API consumida
│   ├── package.json
│   ├── src/                             # Código TypeScript generado
│   │   ├── api.service.ts              # HttpClient service con endpoints
│   │   └── models/                      # DTOs TypeScript
│   └── lib-generated/
│       └── dist/                        # Resultado de ng build (prepare-apis-generated.js)
└── servicio-notificaciones/
    └── ...

asyncapi-generated/                      # Para APIs asíncronas
└── backToFront/
    └── serverpush/
        └── client/                      # Librerías Angular ServerPush
```

### Uso en componentes tras prepare-apis-generated.js

```typescript
// Las librerías quedan instaladas como dependencias locales del proyecto:
import { CuentasApiService } from 'servicio-cuentas';
import { CuentaDTO } from 'servicio-cuentas/models';
```

## Estructura de proyecto

```
src/app/
├── app.module.ts
├── app-routing.module.ts
├── core/                          # Servicios singleton, guards, interceptors
│   ├── core.module.ts
│   ├── guards/
│   │   └── auth.guard.ts
│   ├── interceptors/
│   │   ├── auth.interceptor.ts
│   │   └── error.interceptor.ts
│   └── services/
│       ├── auth.service.ts
│       └── notificaciones.service.ts
├── shared/                        # Módulo compartido (reutilizable)
│   ├── shared.module.ts
│   ├── components/
│   │   ├── loading/
│   │   └── error-message/
│   ├── pipes/
│   │   └── fecha-formato.pipe.ts
│   └── directives/
│       └── autofocus.directive.ts
└── features/                      # Feature modules (lazy loaded)
    ├── dashboard/
    │   ├── dashboard.module.ts
    │   ├── dashboard-routing.module.ts
    │   └── components/
    │       ├── dashboard-page/
    │       └── resumen-card/
    └── clientes/
        ├── clientes.module.ts
        ├── clientes-routing.module.ts
        ├── components/
        ├── services/
        └── models/
```

## Routing con Lazy Loading

```typescript
const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  {
    path: 'dashboard',
    loadChildren: () => import('./features/dashboard/dashboard.module')
      .then(m => m.DashboardModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'clientes',
    loadChildren: () => import('./features/clientes/clientes.module')
      .then(m => m.ClientesModule),
    canActivate: [AuthGuard],
    canLoad: [AuthGuard]
  },
  { path: '**', redirectTo: 'dashboard' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
```

## Servicio con HttpClient

```typescript
@Injectable({ providedIn: 'root' })
export class ClienteService {
  private readonly apiUrl = `${environment.apiBaseUrl}/api/v1/clientes`;

  constructor(private http: HttpClient) {}

  listar(page = 0, size = 20): Observable<Page<ClienteDTO>> {
    return this.http.get<Page<ClienteDTO>>(this.apiUrl, {
      params: { page: page.toString(), size: size.toString() }
    });
  }

  obtener(id: string): Observable<ClienteDTO> {
    return this.http.get<ClienteDTO>(`${this.apiUrl}/${id}`);
  }

  crear(request: CreateClienteRequest): Observable<ClienteDTO> {
    return this.http.post<ClienteDTO>(this.apiUrl, request);
  }

  actualizar(id: string, request: UpdateClienteRequest): Observable<ClienteDTO> {
    return this.http.put<ClienteDTO>(`${this.apiUrl}/${id}`, request);
  }

  eliminar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
```

## Interceptor de Autenticación

```typescript
@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getToken();
    if (token) {
      req = req.clone({
        setHeaders: {
          'Authorization': `Bearer ${token}`,
          'X-BBVA-User-Id': this.authService.getUserId()
        }
      });
    }
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          this.authService.logout();
        }
        return throwError(() => error);
      })
    );
  }
}
```

## Guard de Autenticación

```typescript
@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate, CanLoad {

  constructor(private auth: AuthService, private router: Router) {}

  canActivate(): Observable<boolean> {
    return this.auth.isAuthenticated$.pipe(
      tap(ok => { if (!ok) this.router.navigate(['/login']); })
    );
  }

  canLoad(): Observable<boolean> {
    return this.canActivate();
  }
}
```

## Environments

```typescript
// environment.ts (dev)
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080',
  sseUrl: 'http://localhost:8080/sse'
};

// environment.prod.ts
export const environment = {
  production: true,
  apiBaseUrl: '/api',
  sseUrl: '/sse'
};
```

## Componente Thin3 (patrón típico)

```typescript
@Component({
  selector: 'app-cliente-lista',
  templateUrl: './cliente-lista.component.html',
  styleUrls: ['./cliente-lista.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ClienteListaComponent implements OnInit, OnDestroy {
  clientes$!: Observable<ClienteDTO[]>;
  loading$ = new BehaviorSubject<boolean>(true);
  private destroy$ = new Subject<void>();

  constructor(private clienteService: ClienteService) {}

  ngOnInit(): void {
    this.clientes$ = this.clienteService.listar().pipe(
      map(page => page.content),
      tap(() => this.loading$.next(false)),
      takeUntil(this.destroy$)
    );
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
```

## Proxy config (desarrollo local)

```json
{
  "/api": {
    "target": "http://localhost:8080",
    "secure": false,
    "changeOrigin": true
  },
  "/sse": {
    "target": "http://localhost:8080",
    "secure": false,
    "changeOrigin": true
  }
}
```

Usar con: `ng serve --proxy-config proxy.conf.json`
