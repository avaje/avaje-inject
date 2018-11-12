# kanuka
Kanuka dependency injection - http://kanuka.io

## Similar to Dagger (https://google.github.io/dagger/)

- Uses Java annotation processing for dependency injection
- Generates source code
- Avoids any use of reflection or classpath scanning (so low cost and fast startup)
- Aiming to be a `Library only` (a DI library and that's it)


## Differences to Dagger 

- Aimed for server side development use cases rather than Andriod
- Supports lifecycle methods with `@PostConstruct` and `@PreDestory`
- Supports `@Factory` and `@Bean`
- Provides API to obtain all bean instances that implement an interface  
- Provides API to obtain all bean instances that have an annotation
- Kanuka should be much better to integrate with server side web frameworks like Rapidoid, Spark etc
  
  
## Plans

- Review integration with properties (like Spring `@Value`) and sticky property reloading


## Differences to Micronaut DI (https://docs.micronaut.io/latest/guide/index.html#ioc)

- Kanuka generates source code rather that bytecode
- Kanuka generates construction order at compile time
- Kanuka is aiming to be a `Library only` (so a DI library and that's it without any dependencies)
- Kanuka is expected to stay a lot smaller with minimal dependencies 
- Kanuka is expected to be a lot faster (due to the pre-ordering of construction in generated code)

## Differences to Spring DI (https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/core.html)

- Kanuka only has Singleton scope
- Kanuka doesn't yet have support for something like `@Value`
- Kanuka provides similar DI features but with code generation via APT
  and hence much lower startup time and much lower cost (so better suited to micro-services). 
- No use of reflection or scanning even for multi-module dependency injection 


## Questions

- Add support for reading configuration / application.yml properties etc ?
- Add "Sticky" configuration support (aka reloading config fire PostConstruct)
