[![Discord](https://img.shields.io/discord/1074074312421683250?color=%237289da&label=discord)](https://discord.gg/Qcqf9R27BR)
[![Build](https://github.com/avaje/avaje-inject/actions/workflows/build.yml/badge.svg)](https://github.com/avaje/avaje-inject/actions/workflows/build.yml)
[![JDK EA](https://github.com/avaje/avaje-inject/actions/workflows/jdk-ea.yml/badge.svg)](https://github.com/avaje/avaje-inject/actions/workflows/jdk-ea.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/avaje/avaje-inject/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/io.avaje/avaje-inject.svg?label=Maven%20Central)](https://mvnrepository.com/artifact/io.avaje/avaje-inject)
[![javadoc](https://javadoc.io/badge2/io.avaje/avaje-inject/javadoc.svg?color=purple)](https://javadoc.io/doc/io.avaje/avaje-inject)

# [Avaje-Inject](https://avaje.io/inject)
APT-based dependency injection for server-side developers - https://avaje.io/inject
## Quick Start
#### 1. Add avaje-inject as a dependency.
```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-inject</artifactId>
  <version>${avaje.inject.version}</version>
</dependency>
```
#### 2. Add avaje-inject-generator annotation processor as a dependency with provided scope.
```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-inject-generator</artifactId>
  <version>${avaje.inject.version}</version>
  <scope>provided</scope>
</dependency>
```

#### 3. Create a Bean Class annotated with @Singleton
```java
@Singleton
public class Example {

 private DependencyClass d1;
 private DependencyClass2 d2;

  // Dependencies must be annotated with singleton,
  // or else be provided from another class annotated with @Factory
  public Example(DependencyClass d1, DependencyClass2 d2) {
    this.d1 = d1;
    this.d2 = d2;
  }
}
```
Example factory class:
```java
@Factory
public class ExampleFactory {
  @Bean
  public DependencyClass2 bean() {
    return new DependencyClass2();
  }
}
```

#### 4. Use BeanScope to wire and retrieve the beans and use them however you wish.
```java
BeanScope beanScope = BeanScope.builder().build()
Example ex = beanScope.get(Example.class);
```

### Java Module Usage
When working with Java modules you need to add a `provides` statement in your `module-info.java` with the generated class.
```java
import io.avaje.inject.spi.InjectExtension;

module org.example {

  requires io.avaje.inject;
  // you must define the fully qualified class name of the generated classes. if you use an import statement, compilation will fail
  provides InjectExtension with org.example.ExampleModule;
}
```

### Generated Wiring Class
The inject annotation processor determines the dependency wiring order and generates an `AvajeModule` class that calls all the generated DI classes.

```java
@Generated("io.avaje.inject.generator")
@InjectModule
public final class ExampleModule implements AvajeModule {

  private Builder builder;

  @Override
  public Class<?>[] classes() {
    return new Class<?>[] {
      org.example.DependencyClass.class,
      org.example.DependencyClass2.class,
      org.example.Example.class,
      org.example.ExampleFactory.class,
    };
  }

  /**
   * Creates all the beans in order based on constructor dependencies. The beans are registered
   * into the builder along with callbacks for field/method injection, and lifecycle
   * support.
   */
  @Override
  public void build(Builder builder) {
    this.builder = builder;
    // create beans in order based on constructor dependencies
    // i.e. "provides" followed by "dependsOn"
    build_example_ExampleFactory();
    build_example_DependencyClass();
    build_example_DependencyClass2();
    build_example_Example();
  }

  @DependencyMeta(type = "org.example.ExampleFactory")
  private void build_example_ExampleFactory() {
    ExampleFactory$DI.build(builder);
  }

  @DependencyMeta(type = "org.example.DependencyClass")
  private void build_example_DependencyClass() {
    DependencyClass$DI.build(builder);
  }

  @DependencyMeta(
      type = "org.example.DependencyClass2",
      method = "org.example.ExampleFactory$DI.build_bean", // factory method
      dependsOn = {"org.example.ExampleFactory"}) //factory beans naturally depend on the factory
  private void build_example_DependencyClass2() {
    ExampleFactory$DI.build_bean(builder);
  }

  @DependencyMeta(
      type = "org.example.Example",
      dependsOn = {"org.example.DependencyClass", "org.example.DependencyClass2"})
  private void build_example_Example() {
    Example$DI.build(builder);
  }
}
```

## Similar to [Dagger](https://google.github.io/dagger/)

- Uses Java annotation processing for dependency injection
- Generates source code
- Avoids any use of reflection or classpath scanning (so low overhead and fast startup)

## Differences to Dagger

- Specifically aimed for server-side development (rather than Android)
- Supports "component testing" via `avaje-inject-test` and `@InjectTest`
- Provides API to obtain all bean instances that implement an interface
- Lifecycle methods with `@PostConstruct` and `@PreDestroy`
- Spring-like factory classes with `@Factory` and `@Bean`
- Conditional Wiring based on active profiles or existing beans/properties

## DI Framework comparison

|  Avaje | Dagger | Spring
| :--- | :---  | :---  |
| [@Singleton](https://avaje.io/inject/#singleton) | @Singleton | @Component, @Service, @Repository |
| [Provider&lt;T>](https://avaje.io/inject/#provider) | Provider&lt;T> | FactoryBean&lt;T>
| [@Inject](https://avaje.io/inject/#inject) | @Inject | @Inject, @Autowired
| [@Inject @Nullable](https://avaje.io/inject/#nullable) or [@Inject Optional&lt;T>](https://avaje.io/inject/#optional) | @Inject @Nullable | @Autowired(required=false)
| [@Qualifier/@Named](https://avaje.io/inject/#qualifiers) | @Qualifier/@Named | @Qualifier
| [@AssistFactory](https://avaje.io/inject/#assistInject) | @AssistedFactory | - |
| [@PostConstruct](https://avaje.io/inject/#post-construct) | - | @PostConstruct
| [@PreDestroy](https://avaje.io/inject/#pre-destroy) | - | @PreDestroy
| [@Factory and @Bean](https://avaje.io/inject/#factory) | - | @Configuration and @Bean
| [@RequiresBean and @RequiresProperty](https://avaje.io/inject/#conditional) | - | @Conditional
| [@Lazy](https://avaje.io/inject/#lazy) | - | @Lazy
| [@Primary](https://avaje.io/inject/#primary) | - | @Primary
| [@Secondary](https://avaje.io/inject/#secondary) | - | @Fallback
| [@InjectTest](https://avaje.io/inject/#component-testing) | - | @SpringBootTest
