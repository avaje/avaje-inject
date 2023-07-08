[![Build](https://github.com/avaje/avaje-inject/actions/workflows/build.yml/badge.svg)](https://github.com/avaje/avaje-inject/actions/workflows/build.yml)
[![JDK EA](https://github.com/avaje/avaje-inject/actions/workflows/jdk-ea.yml/badge.svg)](https://github.com/avaje/avaje-inject/actions/workflows/jdk-ea.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/avaje/avaje-inject/blob/master/LICENSE)
[![Maven Central : avaje-inject](https://img.shields.io/maven-central/v/io.avaje/avaje-inject.svg?label=Maven%20Central)](https://maven-badges.herokuapp.com/maven-central/io.avaje/avaje-inject)
[![Discord](https://img.shields.io/discord/1074074312421683250?color=%237289da&label=discord)](https://discord.gg/Qcqf9R27BR)

# [Avaje-Inject](https://avaje.io/inject)
APT based dependency injection for server side developers - https://avaje.io/inject
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
import io.avaje.inject.spi.Module;

module org.example {

  requires io.avaje.inject;
  // you must define the fully qualified class name of the generated classes. if you use an import statement, compilation will fail
  provides Module with org.example.ExampleModule;
}
```

## Similar to [Dagger](https://google.github.io/dagger/)

- Uses Java annotation processing for dependency injection
- Generates source code
- Avoids any use of reflection or classpath scanning (so low overhead and fast startup)

## Differences to Dagger

- Aimed specifically for server-side development (rather than Android)
- Supports lifecycle methods with `@PostConstruct` and `@PreDestory`
- Supports `@Factory` and `@Bean`
- Provides API to obtain all bean instances that implement an interface
- Provides API to obtain all bean instances that have an annotation
- Integration with server-side web frameworks Javalin, Helidon

## Spring DI

For comparison with Spring DI look at https://avaje.io/inject/#spring


## Generated Code

### DI classes

DI classes will be generated to call the constructors for annotated type/factory methods. Below is the class generated for the `Example` class in the above quickstart.

```java
@Generated("io.avaje.inject.generator")
public final class Example$DI  {

  /**
   * Create and register Example.
   */
  public static void build(Builder builder) {
    if (builder.isAddBeanFor(Example.class)) {
      var bean = new Example(builder.get(DependencyClass.class,"!d1"), builder.get(DependencyClass2.class,"!d2"));
      builder.register(bean);
    }
  }
}
```

### Generated Wiring Class
The inject annotation processor determines the dependency wiring order and generates a `Module` class that calls all the generated DI classes.

```
@Generated("io.avaje.inject.generator")
@InjectModule
public final class ExampleModule implements Module {

  private Builder builder;

  @Override
  public Class<?>[] classes() {
    return new Class<?>[] {
      org.example.DependencyClass2.class,
      org.example.Example.class,
      org.example.ExampleFactory.class,
    };
  }

  /**
   * Creates all the beans in order based on constructor dependencies. The beans are registered
   * into the builder along with callbacks for field injection, method injection, and lifecycle
   * support.
   */
  @Override
  public void build(Builder builder) {
    this.builder = builder;
    // create beans in order based on constructor dependencies
    // i.e. "provides" followed by "dependsOn"
    build_example_ExampleFactory();
    build_example_DependencyClass2();
    build_example_Example();
  }

  @DependencyMeta(type = "org.example.ExampleFactory")
  private void build_example_ExampleFactory() {
    ExampleFactory$DI.build(builder);
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
