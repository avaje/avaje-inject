# DInject
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
- Integration with server side web frameworks like Rapidoid, Sparkjava, Javlin etc
  

## Differences to Micronaut DI (https://docs.micronaut.io/latest/guide/index.html#ioc)

- DInject generates source code rather that bytecode
- DInject generates construction order at compile time
- DInject is aiming to be a `Library only` (so a DI library and that's it without any dependencies)
- DInject is expected to stay a lot smaller with minimal dependencies 
- DInject is expected to be a lot faster (due to the pre-ordering of construction in generated code)

## Differences to Spring DI (https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/core.html)

- DInject only has Singleton scope
- DInject doesn't yet have support for something like `@Value`
- DInject provides similar DI features but with code generation via APT
  and hence much lower startup time and much lower cost (so better suited to micro-services). 
- No use of reflection or scanning even for multi-module dependency injection 


## Plans

- Additionally library for reading configuration / application.yaml 
- Configuration supports onChange listening (aka dynamic configuration API)
