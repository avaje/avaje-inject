# Avaje Inject
APT based dependency injection for server side developers - https://avaje.io/inject

### Example module use

```java
module org.example {

  requires io.avaje.inject;

  // register org.example._DI$BeanContextFactory from generated sources
  provides io.avaje.inject.spi.BeanContextFactory with org.example._DI$BeanContextFactory;
}
```

## Similar to Dagger (https://google.github.io/dagger/)

- Uses Java annotation processing for dependency injection
- Generates source code
- Avoids any use of reflection or classpath scanning (so low overhead and fast startup)
- A `Library only` (a DI library and that's it ~25k in size)


## Differences to Dagger

- Aimed specifically for server side development (rather than Andriod)
- Supports lifecycle methods with `@PostConstruct` and `@PreDestory`
- Supports `@Factory` and `@Bean`
- Provides API to obtain all bean instances that implement an interface
- Provides API to obtain all bean instances that have an annotation
- Integration with server side web frameworks like Rapidoid, Sparkjava, Javlin etc


## Differences to Micronaut DI (https://docs.micronaut.io/latest/guide/index.html#ioc)

- Generates source code rather that bytecode
- Generates construction order at compile time
- Aiming to be a `Library only` (so a DI library and that's it without any dependencies)
- Expected to stay a lot smaller with minimal dependencies
- Expected to be faster due to the pre-ordering of construction in generated code

## Differences to Spring DI (https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/core.html)

- Avaje inject only has Singleton scope
- Avaje inject doesn't support `@Value` (use avaje config)
- Avaje inject provides similar DI features but with code generation via APT
  and hence much lower startup time and much lower cost (so better suited to micro-services).
- No use of reflection or scanning even for multi-module dependency injection


## Plans

- Additionally library for reading configuration / application.yaml
- Configuration supports onChange listening (aka dynamic configuration API)
