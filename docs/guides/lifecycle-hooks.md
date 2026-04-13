# Lifecycle Hooks

Initialize and cleanup beans.

## Post-Construct

Run code after bean is created:

```java
@Singleton
public class Service {

  @PostConstruct
  public void init() {
    System.out.println("Service initialized");
  }
}
```

## Pre-Destroy

Run code before bean is destroyed:

```java
@Singleton
public class Service {

  @PreDestroy
  public void shutdown() {
    System.out.println("Service shutting down");
  }
}
```

## Next Steps

- Learn [qualifiers](qualifiers.md)
