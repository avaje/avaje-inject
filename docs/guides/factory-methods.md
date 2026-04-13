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

## Next Steps

- Learn [lifecycle hooks](lifecycle-hooks.md)
