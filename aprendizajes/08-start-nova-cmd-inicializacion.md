# start-nova-cmd.bat — Inicialización del entorno NOVA

> Fuente: `nova-le/start-nova-cmd.bat` (análisis completo)

## Qué hace el script de arranque

El `start-nova-cmd.bat` es el **punto de entrada para Windows**. Configura el entorno
y vincula todos los generadores y el CLI antes de poder usar `nova`.

## Secuencia de inicialización

### Fase 1: Configuración de variables

```bat
SET NOVA_HOME=%~dp0                                    # Directorio del script = raíz nova-le
SET PATH=%NOVA_HOME%nova-deps/yarn-global/bin;%NOVA_HOME%nodejs/;%PATH%
```

### Fase 2: Configuración de Yarn

```bat
yarn config set prefix %NOVA_HOME%nova-deps/yarn-global
yarn config set cache-folder %NOVA_HOME%nova-deps/yarn-cache
```

### Fase 3: Vinculación de módulos (yarn link)

El script vincula secuencialmente cada componente, mostrando progreso:

| Paso | Componente | Progreso | Acción |
|------|-----------|----------|--------|
| 1 | nova-cli | 25% | `cd nova-cli && yarn link` |
| 2 | nova-le-updater | 50% | `cd nova-le-updater && yarn link` |
| 3 | generator-nova | 60% | `cd generators/generator-nova && yarn link` |
| 4 | generator-thin2 | 70% | `cd generators/generator-thin2 && yarn link` |
| 5 | generator-thin3 | 75% | `cd generators/generator-thin3 && yarn link` |
| 6 | generator-asyncapi | 80% | Copia `.npmrc` a subcarpetas de templates |
| 7 | generator-behavior-test | 90% | `cd generators/generator-behavior-test && yarn link` |

### Fase 4: Copia de .npmrc para AsyncAPI

```bat
copy "%NOVA_HOME%generators\generator-asyncapi\.npmrc" "%NOVA_HOME%generators\generator-asyncapi\generator\"
cd %NOVA_HOME%generators\generator-asyncapi\templates
FOR /D %%f IN (*) DO @copy "..\.npmrc" %%f
```

Cada template de AsyncAPI necesita su propio `.npmrc` para poder resolver dependencias del
Artifactory de NOVA.

### Fase 5: Validación de errores

El script verifica si hubo errores en cada `yarn link` y muestra warnings:

```
⚠ CLI             → [error message if any]
⚠ UPDATER         → [error message if any]
⚠ GEN_NOVA        → [error message if any]
⚠ GEN_THIN        → [error message if any]
⚠ GEN_THIN3       → [error message if any]
⚠ GEN_ASYNCAPI    → [error message if any]
⚠ GEN_BH_TEST     → [error message if any]
```

### Fase 6: Verificación de versión (online)

Opcionalmente ejecuta `nova version-check` para verificar si hay nueva versión del CLI
disponible en el servidor de NOVA.

### Fase 7: Prompt listo

Si todo va bien, abre un prompt de Windows con `nova` disponible en el PATH.

## stop-nova-cmd.bat

Script complementario que:
1. Mata todos los procesos Java del runtime
2. Para PostgreSQL
3. Limpia la consola

## Implicaciones para desarrollo

1. **Primer uso obligatorio**: Hay que ejecutar `start-nova-cmd.bat` antes de usar cualquier
   comando `nova` (vincula los módulos)
2. **Conexión a intranet**: Se recomienda para verificación de versiones
3. **Yarn vs NPM**: El CLI usa **Yarn** internamente para gestión de dependencias
4. **Carpeta nova-deps/**: Se crea automáticamente para Yarn cache y binarios globales
5. **No es persistente**: Hay que re-ejecutar si se cierra la ventana de comandos
