# Creating Beans with Avaje Inject

How to create and annotate beans with avaje-inject.

## Basic Bean

Mark a class as a singleton bean:

```java
import io.avaje.inject.Bean;

@Bean
public class UserService {
  public User findById(long id) {
    return new User(id, "John");
  }
}
```

The `@Bean` annotation makes it a singleton - one instance for the application.

## Implementing Interfaces

Beans typically implement interfaces:

```java
public interface UserService {
  User findById(long id);
}

@Bean
public class UserServiceImpl implements UserService {
  @Override
  public User findById(long id) {
    return new User(id, "John");
  }
}
```

## Scopes

Control bean lifecycle:

```java
@Singleton          // One instance (default)
public class Single { }

@Prototype          // New instance each time
public class Multi { }

@ApplicationScoped  // Application lifetime
public class AppBeans { }
```

## Next Steps

- Learn [dependency injection](dependency-injection.md)
- Use [factory methods](factory-methods.md)
