# Native Images with Avaje Inject

Building GraalVM native images with avaje-inject.

## Setup

Add native image plugin to `pom.xml`:

```xml
<plugin>
  <groupId>org.graalvm.buildtools</groupId>
  <artifactId>native-maven-plugin</artifactId>
  <version>0.10.0</version>
</plugin>
```

## Build

```bash
mvn -Pnative clean package
```

Avaje Inject provides automatic GraalVM metadata.

## Performance

Native images with inject offer:

- **Startup**: < 50ms
- **Memory**: 30-50MB
- **No warm-up**: Full speed immediately
