# Using Qualifiers

Handle multiple bean implementations.

## Named Qualifier

```java
@Singleton
@Named("primary")
public class PrimaryService implements Service { }

@Singleton
@Named("secondary")
public class SecondaryService implements Service { }

@Singleton
public class Client {
  private final Service primary;

  public Client(@Named("primary") Service service) {
    this.primary = service;
  }
}
```

## Strongly Typed Qualifiers (Recommended)

For better type safety and IDE support, create custom qualifier annotations:

```java
@Qualifier
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
public @interface Blue { }

@Qualifier
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
public @interface Green { }
```

Then use them on beans and injections:

```java
@Singleton
@Blue
public class PrimaryService implements Service { }

@Singleton
@Green
public class SecondaryService implements Service { }

@Singleton
public class Client {
  private final Service primary;

  public Client(@Blue Service service) {
    this.primary = service;
  }
}
```

**Why prefer strongly typed qualifiers?** They provide compile-time type checking and IDE autocomplete support, avoiding the "Stringly typed" errors that can occur with `@Named`.

## Next Steps

- See [testing](testing.md)
