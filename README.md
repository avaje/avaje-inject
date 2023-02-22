[![Build](https://github.com/avaje/avaje-inject/actions/workflows/build.yml/badge.svg)](https://github.com/avaje/avaje-inject/actions/workflows/build.yml)
[![JDK EA](https://github.com/avaje/avaje-inject/actions/workflows/jdk-ea.yml/badge.svg)](https://github.com/avaje/avaje-inject/actions/workflows/jdk-ea.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/avaje/avaje-inject/blob/master/LICENSE)
[![Maven Central : avaje-inject](https://maven-badges.herokuapp.com/maven-central/io.avaje/avaje-inject/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.avaje/avaje-inject)
[![Discord](https://img.shields.io/discord/1074074312421683250?color=%237289da&label=discord)](https://discord.gg/kzew4q8A)

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
If there are other annotation processors and they are specified via `maven-compiler-plugin`, then we add avaje-inject-generator there instead.
```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <configuration>
    <annotationProcessorPaths> <!-- All annotation processors specified here -->
      <path>
          <groupId>io.avaje</groupId>
          <artifactId>avaje-inject-generator</artifactId>
          <version>${avaje.inject.version}</version>
      </path>
      <path>
          ... other annotation processor ...
      </path>
    </annotationProcessorPaths>
  </configuration>
</plugin>
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
  public DependencyClass2() {
    return new DependencyClass2();
  }
}
```

#### 4. Use BeanScope to wire and retrieve the beans and use however you wish.
```java
BeanScope beanScope = BeanScope.builder().build()
Example ex = beanScope.get(Example.class);
```

### Example module use
```java
module org.example {

  requires io.avaje.inject;

  provides io.avaje.inject.spi.Module with org.example.ExampleModule;
}
```

## Similar to Dagger (https://google.github.io/dagger/)

- Uses Java annotation processing for dependency injection
- Generates source code
- Avoids any use of reflection or classpath scanning (so low overhead and fast startup)
- A `Library only` (a DI library and that's it. ~25k in size)


## Differences to Dagger

- Aimed specifically for server side development (rather than Andriod)
- Supports lifecycle methods with `@PostConstruct` and `@PreDestory`
- Supports `@Factory` and `@Bean`
- Provides API to obtain all bean instances that implement an interface
- Provides API to obtain all bean instances that have an annotation
- Integration with server side web frameworks Javalin, Helidon

## Spring DI

For comparison with Spring DI look at https://avaje.io/inject/#spring
