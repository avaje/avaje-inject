# Dependency Injection with Avaje Inject

How to inject dependencies into beans.

## Constructor Injection

Inject through constructor (recommended):

```java
@Singleton
public class OrderService {
  private final UserService userService;
  private final PaymentService paymentService;

  public OrderService(UserService userService, PaymentService paymentService) {
    this.userService = userService;
    this.paymentService = paymentService;
  }
}
```

If a bean class has more than one constructor, annotate the constructor Avaje
Inject should use with `@Inject`. This is common when a class has a package-private
test constructor in addition to the normal DI constructor.

```java
@Singleton
class MetricsReporter {

  @Inject
  MetricsReporter(Configuration config, Optional<GraphiteReporter> reporter) {
    this(config, reporter.map(MetricsReporter::scheduledTask).orElse(null));
  }

  MetricsReporter(Configuration config, ScheduledTask task) {
    // test-friendly constructor
  }
}
```

## Multiple Implementations

Use `@Named` qualifier:

```java
public interface Logger { }

@Singleton
@Named("file")
public class FileLogger implements Logger { }

@Singleton
@Named("console")
public class ConsoleLogger implements Logger { }

@Singleton
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
