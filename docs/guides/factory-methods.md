# Factory Methods

Create beans using factory methods.

## Basic Factory

```java
import io.avaje.inject.Factory;
import io.avaje.inject.Bean;

@Factory
public class DatabaseFactory {
  
  @Bean
  public DataSource createDataSource() {
    return new HikariDataSource(...);
  }
  
  @Bean
  public Database createDatabase(DataSource ds) {
    return new Database(ds);
  }
}
```

The factory methods create and configure beans.

## Optional Beans

Factory methods can return `Optional<T>`. When the optional is present, Avaje
Inject registers the contained `T` bean. When it is empty, no bean is registered.
Consumers can inject `Optional<T>` to model integrations that may be disabled.

```java
@Factory
class MetricsConfig {

  @Bean
  Optional<GraphiteReporter> graphiteReporter(Configuration config) {
    if (!config.enabled("metrics.graphite.enabled", false)) {
      return Optional.empty();
    }
    return Optional.of(GraphiteReporter.builder().build());
  }
}

@Singleton
class MetricsReporter {

  private final Optional<GraphiteReporter> reporter;

  MetricsReporter(Optional<GraphiteReporter> reporter) {
    this.reporter = reporter;
  }
}
```

An `@Bean Optional<T>` method does not register `Optional<T>` as the bean type. It
conditionally registers `T`.

## Bean Creation Order

Bean creation order follows real dependencies. If one bean must be initialized
before another, express that by accepting the dependency as a constructor or
factory-method parameter.

```java
@Factory
class DatabaseConfig {

  @Bean
  Database database(OpenTelemetry openTelemetry, DataSource dataSource) {
    return Database.builder()
      .dataSource(dataSource)
      .build();
  }
}
```

In this example `OpenTelemetry` is created before the `Database`. This is useful
when the database startup reads OpenTelemetry global state. Avoid reverse or
artificial dependencies that create cycles; the dependency direction should match
the required initialization order.

## Next Steps

- Learn [lifecycle hooks](lifecycle-hooks.md)
