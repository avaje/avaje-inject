# Using Qualifiers

Handle multiple bean implementations.

## Named Qualifier

```java
@Bean
@Named("primary")
public class PrimaryService implements Service { }

@Bean
@Named("secondary")
public class SecondaryService implements Service { }

@Bean
public class Client {
  private final Service primary;
  
  public Client(@Named("primary") Service service) {
    this.primary = service;
  }
}
```

## Next Steps

- See [testing](testing.md)
