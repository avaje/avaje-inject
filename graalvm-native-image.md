# GraalVM Native Image

avaje-inject is designed to work with GraalVM native image out of the box. No additional
dependencies or native-image configuration files are required.

## How it works

avaje-inject uses **compile-time code generation** via the `inject-generator` annotation
processor. At build time it generates:

- **`*Module` classes** — explicit wiring code that constructs and injects all beans in
  dependency order, with no reflection
- **`*$DI` classes** — per-bean classes containing the explicit constructor calls, field
  injection, and lifecycle callbacks
- **`META-INF/services/io.avaje.inject.spi.InjectExtension`** — the service file that
  `ServiceLoader` uses to discover the generated module at startup

Because all wiring is expressed as plain Java source code, no reflection is used and
it is just plain java code and nothing special for the native image compiler.

### Lazy beans

`@Lazy` beans are also handled at compile time. The generator produces a generated proxy
subclass that defers construction until first use. No dynamic proxy or runtime bytecode
generation is involved.

### AOP proxies

When avaje-inject generates AOP proxies (e.g. for `@Aspect` beans),
these are also generated as plain source code by the annotation processor. The result is a
concrete subclass compiled into the application — fully visible to the native image compiler.

