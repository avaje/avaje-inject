# Dependency Injection with Avaje Inject

How to inject dependencies into beans.

## Constructor Injection

Inject through constructor (recommended):

```java
@Bean
public class OrderService {
  private final UserService userService;
  private final PaymentService paymentService;
  
  public OrderService(UserService userService, PaymentService paymentService) {
    this.userService = userService;
    this.paymentService = paymentService;
  }
}
```

## Multiple Implementations

Use `@Named` qualifier:

```java
public interface Logger { }

@Bean
@Named("file")
public class FileLogger implements Logger { }

@Bean
@Named("console")  
public class ConsoleLogger implements Logger { }

@Bean
public class Service {
  private final Logger fileLogger;
  
  public Service(@Named("file") Logger logger) {
    this.fileLogger = logger;
  }
}
```

## Next Steps

- See [factory methods](factory-methods.md)
- Learn [lifecycle hooks](lifecycle-hooks.md)
