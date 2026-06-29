# Demo 5: Internacionalización (ES / EN) con selector de idioma

**Sprint de desarrollo**: i18n en la UI con cambio de idioma en caliente
**Duración estimada**: 3-4 horas
**Complejidad**: Intermedio
**Agentes NOVA**: nova-architect, nova-frontend-gen, nova-api-integr, nova-release-mgr

> Demo de desarrollo. Los agentes NOVA escriben código y el resultado es un **efecto visible en la UI**
> (toda la interfaz cambia de idioma). No modifica la lógica de negocio.

---

## 1. Objetivo

Internacionalizar `guarantees-ui` con soporte **Español / Inglés** y un **selector de idioma** en la
barra superior que traduce la interfaz **en caliente** (sin recargar) y **persiste** la elección.

## 2. Contexto

La i18n es una mejora de plataforma transversal y muy demostrable: el agente de frontend extrae las
cadenas a catálogos, conecta un servicio de traducción reactivo y localiza fechas/números. El cambio de
idioma en vivo es un efecto de UI inmediato y claro para enseñar al inicio y al final.

## 3. Alcance

### Frontend (`nova-frontend-gen`)

- [ ] Integrar una librería de i18n en runtime (`@ngx-translate/core` con `@ngx-translate/http-loader`)
      para permitir cambio de idioma sin recargar.
- [ ] Crear catálogos `assets/i18n/es.json` y `assets/i18n/en.json` y **extraer todas las cadenas
      visibles** (tabla, formularios, diálogos, botones, validaciones, badges).
- [ ] Añadir un **selector de idioma** (ES/EN) en la barra; aplicar el idioma y persistirlo en
      `localStorage` (clave `nova.lang`); idioma por defecto según el navegador.
- [ ] Localizar **fechas y números** con `DatePipe`/`DecimalPipe` y los `LOCALE_ID` registrados (`es`, `en`).
- [ ] Sustituir textos hardcodeados por claves de traducción (`| translate`).

### Integración / contrato (`nova-api-integr`) — opcional

- [ ] Si algún texto proviene del backend (p. ej. mensajes de error), acordar que el backend devuelva
      **claves** (o un campo `code`) y el frontend las traduzca, documentándolo en el contrato de error.

## 4. Aceptación

- El selector cambia el idioma de **toda** la UI al instante, sin recargar.
- No quedan textos sin traducir en las pantallas principales (tabla, formularios, diálogos).
- Fechas y números se formatean según el idioma activo.
- La preferencia persiste tras recargar; idioma inicial según el navegador.
- Tests: servicio/uso de traducción (clave→texto) y render de un componente en ambos idiomas.

## 5. Demostración (estado inicial → final)

| Antes | Después |
|-------|---------|
| La UI está en un único idioma, con textos fijos. | Selector ES/EN en la barra; al cambiarlo, toda la interfaz (incluidos fechas y números) se traduce al vuelo y la elección persiste. |

**Guion**: abrir la app en español → mostrar tabla/formulario → cambiar a inglés en el selector → toda
la UI se traduce al instante → recargar → sigue en inglés.

## 6. Equipo y reparto de trabajo

> Agentes de la organización **NOVA** (`QPaperClip/containers/nova-org`).

| Agente | Adapter | Responsabilidad en este PRD |
|--------|---------|------------------------------|
| **nova-architect** (CTO) | Claude Code | Descompone el PRD, define la estrategia de i18n (catálogos, claves, locales) y, si aplica, el contrato de errores por `code`. Revisa. |
| **nova-frontend-gen** | Antigravity | Integra ngx-translate, crea catálogos, extrae cadenas, añade el selector y la persistencia, localiza fechas/números. Tests front. |
| **nova-api-integr** | Antigravity | (Opcional) Acuerda y documenta que los mensajes de error del backend viajen como `code` traducible. |
| **nova-release-mgr** | Codex | Verifica build + arranque Docker y ejecuta el gate `nova-post-gen-validation`. |

`nova-async-comm` y `nova-ops-monitor` quedan en **standby**.

### Flujo de ejecución

1. **nova-architect** descompone (≤5 sub-tareas) y fija la estrategia de i18n.
2. **nova-frontend-gen** integra la librería, extrae cadenas y añade el selector.
3. **nova-api-integr** (si aplica) ajusta el contrato de errores.
4. **nova-release-mgr** valida y aplica el gate.
5. **nova-architect** aprueba la entrega (PR en rama separada).
