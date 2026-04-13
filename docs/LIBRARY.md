# Avaje Inject Library Definition

Avaje Inject is a compile-time dependency injection framework optimized for server-side Java applications. It generates all bean wiring code at compile time with zero reflection, making it ideal for GraalVM native images and high-performance applications.

## Identity

- **Name**: Avaje Inject
- **Package**: `io.avaje.inject`
- **Description**: Compile-time dependency injection framework with zero reflection for server-side Java applications
- **Category**: Dependency Injection Framework
- **Repository**: https://github.com/avaje/avaje-inject
- **Issues**: https://github.com/avaje/avaje-inject/issues
- **Releases**: https://github.com/avaje/avaje-inject/releases
- **Discord**: https://discord.gg/Qcqf9R27BR

## Version & Requirements

- **Latest Release**: 12.5 (latest stable)
- **Minimum Java Version**: 11+
- **Build Tools**: Maven 3.6+, Gradle 7.0+
- **GraalVM Support**: Yes — Zero reflection, fully compatible with native image compilation

## Dependencies

### Runtime
- **No external dependencies** — Zero runtime dependencies besides Java standard library

### Compile-Time (Build Only)
- **avaje-inject-generator** — Annotation processor that generates DI code (provided scope, not in final JAR)

### Test
- **JUnit 5 (Jupiter)** — Testing framework
- **avaje-inject-test** — Testing support with `@InjectTest` annotation

### Optional
- **avaje-inject-events** — Event publishing and subscription system
- **avaje-inject-aop** — AOP-style interceptors and cross-cutting concerns

## Core Annotations & APIs

### Bean Definition

| Name | Purpose | Example |
|------|---------|---------|
| `@Singleton` | Mark class as application singleton | `@Singleton\npublic class UserService {}` |
| `@Prototype` | Mark class for prototype scope (new instance per injection) | `@Prototype\npublic class RequestContext {}` |
| `@Factory` | Mark class containing factory methods | `@Factory\npublic class BeanFactory {}` |
| `@Bean` | Mark method as bean producer | `@Bean\npublic UserRepository userRepo()` |

### Dependency Injection

| Name | Purpose | Example |
|------|---------|---------|
| `@Inject` | Inject dependencies into constructor, field, or method | `@Inject UserService service` |
| `@Named` | Specify which implementation to inject by name | `@Named("primary") UserService service` |
| `@Primary` | Mark bean as primary implementation | `@Primary\n@Singleton public class PrimaryImpl {}` |

### Lifecycle Management

| Name | Purpose | Example |
|------|---------|---------|
| `@PostConstruct` | Initialize bean after construction | `@PostConstruct void init() {}` |
| `@PreDestroy` | Clean up bean before destruction | `@PreDestroy void shutdown() {}` |

## Features

### ✅ Included (Since v1.0)
- **Compile-time annotation processing** — All DI graph processing happens at compile time
- **Zero reflection** — No runtime reflection for bean discovery or injection
- **Constructor injection** — Type-safe dependency resolution through constructors
- **Field injection** — Compatible with public or package-private fields
- **Factory methods** — `@Factory` and `@Bean` for flexible bean creation
- **Singleton scope** — Application-scoped beans with `@Singleton`
- **Prototype scope** — New-instance-per-use with `@Prototype`
- **Named qualifiers** — Disambiguate multiple implementations with `@Named`
- **Lifecycle hooks** — `@PostConstruct` and `@PreDestroy` for initialization and cleanup
- **Service loader auto-registration** — Generated modules automatically discovered

### ✅ Added in v5.0+
- **Event publishing system** — Optional avaje-inject-events for publish-subscribe pattern
- **AOP interceptors** — Optional avaje-inject-aop for aspect-oriented programming

### ❌ Not Supported
- **Runtime XML configuration** — Code-only, no Spring-like XML config
- **Dynamic proxy creation at runtime** — All wiring is compile-time
- **Circular dependency resolution** — Must be resolved through additional beans or refactoring

**Note**: These limitations are intentional design choices to keep the framework lightweight and GraalVM-friendly.

## Use Cases

### ✅ Perfect For

- Server-side Java applications and microservices
- High-performance systems requiring minimal overhead
- GraalVM native image projects
- Applications where startup time matters
- Type-safe dependency resolution
- Large enterprise applications requiring precise control

**When to choose avaje-inject**: If you want fast compile-time DI with zero reflection and full GraalVM support.

### ❌ Not Recommended For

- Spring applications — If committed to Spring ecosystem, use Spring's DI
- Rapid prototyping with configuration discovery — If you prefer classpath scanning
- Dynamic bean registration at runtime — If you need to add beans after boot

## Quick Start

### Add to Project

#### Maven
```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-inject</artifactId>
  <version>12.5</version>
</dependency>

<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-inject-generator</artifactId>
  <version>12.5</version>
  <scope>provided</scope>
</dependency>

<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-inject-test</artifactId>
  <version>12.5</version>
  <scope>test</scope>
</dependency>
```

#### Gradle
```gradle
implementation 'io.avaje:avaje-inject:12.5'
annotationProcessor 'io.avaje:avaje-inject-generator:12.5'
testImplementation 'io.avaje:avaje-inject-test:12.5'
```

### Minimal Example

```java
@Singleton
public class UserService {
  public String getUser(int id) {
    return "User: " + id;
  }
}

@Singleton
public class UserController {
  private final UserService userService;

  @Inject
  public UserController(UserService userService) {
    this.userService = userService;
  }

  public String showUser(int id) {
    return userService.getUser(id);
  }
}

public class Main {
  public static void main(String[] args) {
    BeanScope scope = BeanScope.builder().build();
    UserController controller = scope.get(UserController.class);
    System.out.println(controller.showUser(1));
  }
}
```

## Common Tasks & Guides

| Task | Difficulty | Guide |
|------|-----------|-------|
| Create your first bean | Beginner | [docs/guides/creating-beans.md](../guides/creating-beans.md) |
| Inject dependencies into beans | Beginner | [docs/guides/dependency-injection.md](../guides/dependency-injection.md) |
| Use factory methods to create beans | Beginner | [docs/guides/factory-methods.md](../guides/factory-methods.md) |
| Configure initialization and cleanup | Intermediate | [docs/guides/lifecycle-hooks.md](../guides/lifecycle-hooks.md) |
| Handle multiple implementations | Intermediate | [docs/guides/qualifiers.md](../guides/qualifiers.md) |
| Write integration tests | Intermediate | [docs/guides/testing.md](../guides/testing.md) |
| Build GraalVM native images | Advanced | [docs/guides/native-image.md](../guides/native-image.md) |

**Full Guides Index**: See [docs/guides/README.md](../guides/README.md)

## API Quick Reference

### Basic Bean Definition

```java
@Singleton
public class UserRepository {
  public User findById(int id) {
    return new User(id, "John");
  }
}

@Singleton
public class UserService {
  private final UserRepository repository;

  @Inject
  public UserService(UserRepository repository) {
    this.repository = repository;
  }

  public User getUser(int id) {
    return repository.findById(id);
  }
}
```

### Factory Methods

```java
@Factory
public class DatabaseFactory {
  
  @Bean
  public DatabaseConnection dbConnection() {
    return new DatabaseConnection("jdbc:mysql://localhost:3306/mydb");
  }
  
  @Bean
  public UserRepository userRepository(DatabaseConnection connection) {
    return new UserRepository(connection);
  }
}
```

### Named Qualifiers

```java
@Singleton
public class PrimaryUserService implements UserService {}

@Singleton
@Named("backup")
public class BackupUserService implements UserService {}

@Singleton
public class UserController {
  private final UserService primary;
  private final UserService backup;

  @Inject
  public UserController(
      UserService primary,
      @Named("backup") UserService backup) {
    this.primary = primary;
    this.backup = backup;
  }
}
```

### Lifecycle Hooks

```java
@Singleton
public class DatabasePool {
  private Connection pool;

  @PostConstruct
  void initialize() {
    pool = createConnectionPool();
  }

  @PreDestroy
  void cleanup() {
    pool.close();
  }
}
```

## Testing

### Integration Testing with @InjectTest

```java
@InjectTest
public class UserServiceIntegrationTest {
  
  @Inject
  private UserService userService;
  
  @Test
  void testGetUser() {
    User user = userService.getUser(1);
    assertNotNull(user);
  }
}
```

**See**: [docs/guides/testing.md](../guides/testing.md)

## Performance Characteristics

- **Startup time**: ~50-200ms (depends on bean count), 5-50ms (native image)
- **Memory footprint**: ~10-50MB (depends on bean count)
- **Injection overhead**: <1ms per bean injection
- **GraalVM native startup**: 5-50ms

## GraalVM Native Image

### Zero-Config Support
- ✅ Works out of the box with no reflection configuration needed
- ✅ All bean wiring determined at compile-time
- ✅ Minimal native image size overhead

### Native Compilation

```bash
mvn clean package -Pnative
```

**See**: [docs/guides/native-image.md](../guides/native-image.md)

## Troubleshooting

### Issue: Bean Not Found

**Symptom**: `java.lang.IllegalStateException: Bean not found`

**Solution**: Ensure bean class is annotated with `@Singleton`, `@Factory`, or `@Bean`.

### Issue: Circular Dependency

**Symptom**: Build fails with circular dependency error

**Solution**: Refactor to break the cycle using setter injection, intermediate bean, or `Optional<>` injection.

## Version History

| Version | Release Date | Major Changes |
|---------|---|---|
| 12.5 | 2026-04 | Latest stable version |
| 12.0 | 2026-01 | Module system improvements |
| 10.0 | 2025-10 | Builder API enhancements |
| 5.0 | 2024-10 | Events and AOP support |
| 1.0 | 2020-01 | Initial release |

## Support & Community

- **GitHub Issues**: [Report bugs](https://github.com/avaje/avaje-inject/issues)
- **GitHub Discussions**: [Ask questions](https://github.com/avaje/avaje-inject/discussions)
- **Discord**: [Chat with community](https://discord.gg/Qcqf9R27BR)
- **Website**: [Documentation](https://avaje.io/inject/)

## AI Agent Instructions

### For Claude, GPT-4, and Web-Based Agents

This `LIBRARY.md` file is your primary reference for Avaje Inject. When answering questions:

1. Check this file first for capabilities and supported features
2. Route to specific guides using URLs in "Common Tasks" section
3. Refer to use cases to determine if Inject fits user's needs
4. Use "Not Supported" section to avoid recommending unsupported features
5. Check performance characteristics for performance questions

**Key Facts**:
- Minimum Java: 11+
- Current version: 12.5
- Compile-time annotation processing, zero reflection
- Zero external runtime dependencies
- Full GraalVM native image support

---

**Template Version**: 1.0  
**Last Updated**: 2026-04-13
