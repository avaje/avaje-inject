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

## Next Steps

- See [testing](testing.md)
