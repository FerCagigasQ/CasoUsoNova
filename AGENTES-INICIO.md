# AGENTES: Inicio de FASE 2 - Delegación

**Fecha:** 2026-06-05  
**Status:** ✅ LISTO PARA COMENZAR  
**Arquitecto NOVA:** Supervisor de FASE 2 y FASE 3

---

## 📋 Información para Todos los Agentes

Habéis recibido vuestras asignaciones en los issues de GitHub:
- NOV-9A, NOV-9B, NOV-9C, NOV-9D, NOV-9E, NOV-9F

Cada issue contiene la especificación completa de lo que debéis hacer.

---

## 🚀 Pasos a Seguir (para CADA agente)

### 1. Clonad el repo
```bash
git clone https://github.com/FerCagigasQ/CasoUsoNova
cd CasoUsoNova
```

### 2. Cread vuestra rama feature
```bash
git checkout -b feature/gdpd-<vuestro-componente>
```

Ejemplos:
- `feature/gdpd-backend` (nova-service-gen)
- `feature/gdpd-frontend` (nova-frontend-gen)
- `feature/gdpd-integration` (nova-api-integr)
- `feature/gdpd-async` (nova-async-comm)
- `feature/gdpd-release` (nova-release-mgr)
- `feature/gdpd-ops` (nova-ops-monitor)

### 3. Trabajad en vuestra rama
- Ejecutad los comandos NOVA especificados en vuestro issue
- Implementad el código necesario
- Haced commits descriptivos
- Actualizad la documentación (docs/)

### 4. Haced push de vuestra rama
```bash
git push origin feature/gdpd-<vuestro-componente>
```

### 5. Cread Pull Request
En GitHub:
- Crear PR desde vuestra rama a `main`
- Descripción clara de qué incluye
- **IMPORTANTE:** Comentad el link del PR en vuestro issue en GitHub

### 6. Arquitecto revisa
- Arquitecto revisa vuestro PR
- Si está bien → aprobado y mergeado
- Si necesita cambios → comentarios → vosotros corregís

---

## 📦 Recursos Disponibles

Todos los recursos necesarios están en el repo:

- **toolchain/** — NOVA CLI 7.8.0 + JDK 11
- **agents/** — Configuración de vuestros agentes
- **aprendizajes/** — 10 docs sobre cómo usar NOVA
- **skills/** — Skills NOVA para Paperclip
- **scripts/** — Scripts de configuración del entorno
- **.paperclip.yaml** — Config de company con vuestras asignaciones
- **docs/arquitectura.md** — Diseño completo del sistema GDPD

---

## ⚠️ Regla Obligatoria

**NUNCA hagáis push directo a `main`.**

El flujo es:
```
rama feature/gdpd-*
       ↓
commits + push
       ↓
Pull Request a main
       ↓
Review del Arquitecto
       ↓
Merge (si está correcto)
```

---

## 📞 Comunicación

### Con el Arquitecto:
- Si tenéis dudas sobre vuestros requerimientos → preguntad en vuestro issue de GitHub
- Cuando hayáis terminado → comentad el link del PR en el issue
- El Arquitecto os responderá con aprobación o cambios necesarios

### Entre agentes:
- Algunos agentes dependen de otros (ej: nova-api-integr depende de nova-service-gen)
- Coordináos si es necesario

---

## ✅ Checklist Antes de Empezar

- [ ] He clonado el repo: `https://github.com/FerCagigasQ/CasoUsoNova`
- [ ] He leído mi issue en GitHub (NOV-9A/B/C/D/E/F)
- [ ] He leído PAPERCLIP-SUBISSUES.md para la especificación completa
- [ ] He creado mi rama `feature/gdpd-*`
- [ ] Entiendo que debo comentar el link del PR en mi issue cuando termine
- [ ] Entiendo la regla: NO push directo a main

---

## 🎯 Timeline Esperado

FASE 2 (vosotros): Semanas próximas  
FASE 3 (Arquitecto): Revisión y merge de todos los PRs  
CIERRE: Cuando todos los 6 PRs estén mergeados → NOV-9 se cierra

---

**Suerte, equipo NOVA! 🚀**

---

Generado por: Arquitecto NOVA  
Supervisión: NOV-9 Crear producto NOVA "Gestión de Pedidos" (GDPD) desde cero
