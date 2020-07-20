# [DInject](https://dinject.io)
APT based dependency injection for server side developers - https://dinject.io

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
- Integration with server side web frameworks Javalin, Helidon

## Spring DI

For comparison with Spring DI look at https://dinject.io/docs
