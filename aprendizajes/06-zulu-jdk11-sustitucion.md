# Zulu JDK 11 — Sustitución en nova-le

> Fuente: `containers/nova-org/toolchain/zulu-jdk11/release`, análisis de `nova-cli.js`, `generator-nova/app/constants.js`

## Versión exacta del Zulu JDK incluido

```
IMPLEMENTOR="Azul Systems, Inc."
IMPLEMENTOR_VERSION="Zulu11.48+21-CA"
JAVA_VERSION="11.0.11"
JAVA_VERSION_DATE="2021-04-20"
OS_ARCH="x86_64"
OS_NAME="Windows"
```

## Por qué sustituir

El nova-le incluye **Java 8** (1.8.121) por defecto en `tools/java/`.
Sin embargo, la mayoría de proyectos NOVA modernos requieren **Java 11**, porque:

1. **Dependencias base**: Java 11 usa `com.bbva.enoa.core.base:9.36.2` (vs `1.4.1` en Java 8)
2. **Generador de APIs**: Para Java 11, se usa generador versión `2.x` (vs `1.x` para Java 8)
3. **Config Server**: Existe versión específica `discovery-configserver-j11.jar`
4. **JDK choices en generator**: `zulu11.48.21` o `amazon-corretto-11.0.11.9.1`

## Proceso de sustitución

```bash
# 1. Respaldar Java 8 original (opcional)
mv $NOVA_HOME/tools/java $NOVA_HOME/tools/java-8-backup

# 2. Copiar Zulu JDK 11 en su lugar
cp -r zulu-jdk11/ $NOVA_HOME/tools/java/

# 3. Verificar
$NOVA_HOME/tools/java/bin/java -version
# → openjdk version "11.0.11" 2021-04-20
# → OpenJDK Runtime Environment Zulu11.48+21-CA (build 11.0.11+9)
```

## Impacto en el CLI

### initializeEnvironment() (nova-cli.js)

```javascript
process.env.JAVA_HOME = path.join(process.env.NOVA_HOME, 'tools', 'java');
```

Tras la sustitución, `JAVA_HOME` apunta automáticamente a Zulu JDK 11 sin cambiar código.

### Generator service (generator.service.js)

```javascript
// Cuando languageVersion es '11.0.11', usa generadores 2.x
if (languageVersion === '11.0.11' || language === 'Angular - Thin3') {
    version = '11.0.11';
    // → generatorVersion = APIGEN_VERSION['7.8.0']['11.0.11'] = '2.9.2'
} else {
    version = 'DEFAULT';
    // → generatorVersion = APIGEN_VERSION['7.8.0']['DEFAULT'] = '1.9.11'
}
```

## Contenido del directorio Zulu JDK 11

```
zulu-jdk11/
├── bin/           # Binarios Java (java.exe, javac.exe, etc.) + DLLs Windows
├── demo/          # Demos JDK
├── jmods/         # Java modules
├── lib/           # Runtime libraries
├── DISCLAIMER
└── release        # Metadata de la distribución
```

## Compatibilidad

| Componente | Java 8 | Java 11 (Zulu) | Notas |
|------------|--------|-----------------|-------|
| API services | ✅ | ✅ (recomendado) | Usa base `9.36.2` |
| Batch | ✅ | ✅ | |
| Daemon | ✅ | ✅ | |
| ActiveMQ | ✅ | ✅ | |
| Config Server | ✅ (.jar) | ✅ (-j11.jar) | JAR específico para Java 11 |
| API Generator | 1.x | 2.x | Versiones distintas |
| Maven | ✅ | ✅ | |
| CDN/Thin3 | N/A | N/A | Usa Node.js, no Java |
