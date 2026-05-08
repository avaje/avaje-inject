# Creating Beans with Avaje Inject

How to create and annotate beans with avaje-inject.

## Basic Bean

Mark a class as a singleton bean:

```java
import jakarta.inject.Singleton;

@Singleton
public class UserService {
  public User findById(long id) {
    return new User(id, "John");
  }
}
```

The `@Singleton` annotation makes it a singleton - one instance for the application.

## Implementing Interfaces

Beans can implement interfaces if desired:

```java
public interface UserService {
  User findById(long id);
}

@Singleton
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
```

## Next Steps

- Learn [dependency injection](dependency-injection.md)
- Use [factory methods](factory-methods.md)
