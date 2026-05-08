# Testing with Postgres, Ebean, and Avaje Inject

This guide shows how to set up robust component/integration tests using Avaje Inject, Ebean ORM, and a real Postgres database (via Testcontainers). It covers using TestEntityBuilder for test data, and handling multiple named databases.

---

## 1. Test Configuration Example

Define a test configuration with `@TestScope` and `@Factory` to provide beans for Postgres, Ebean Database, and TestEntityBuilder.

```java
@TestScope
@Factory
class TestConfiguration {

  @Bean
  PostgresContainer container() {
    return PostgresContainer.builder("17")
      .dbName("testdb")
      .containerName("ut_test_postgres")
      .port(5557)
      .build()
      .start();
  }

  @Bean
  Database database(PostgresContainer container) {
    return container.ebean().builder()
      .name("primary")
      .ddlRun(true)
      .build();
  }

  @Bean
  TestEntityBuilder testEntityBuilder(Database database) {
    return TestEntityBuilder.builder(database).build();
  }
}
```

- The PostgresContainer is started automatically for tests.
- The Database bean is configured for Ebean and wired into your tests.
- TestEntityBuilder helps create and persist test entities populated with random data.

---

## 2. Using TestEntityBuilder

TestEntityBuilder makes it easy to create and persist test data with random values, reducing boilerplate.

**Example:**
```java
@InjectTest
class UserServiceTest {

  @Inject Database database;
  @Inject TestEntityBuilder builder;

  @Test
  void testFindUser() {
    // Persist a random user
    User user = builder.save(User.class);
    // Or: build, customize, then save
    // User user = builder.build(User.class).setActive(true);
    // database.save(user);

    User found = database.find(User.class, user.getId());
    assertEquals(user.getName(), found.getName());
  }
}
```

- See the [Ebean TestEntityBuilder guide](https://github.com/ebean-orm/ebean/blob/HEAD/docs/guides/testing-with-testentitybuilder.md) for advanced usage.

---

## 3. Multiple Named Databases

To test with multiple databases (e.g., @MainDb, @ReportingDb, @ArchiveDb), define multiple beans with custom qualifiers:

```java
@TestScope
@Factory
class MultiDbTestFactory {

  @MainDb
  @Bean
  Database mainDb(PostgresContainer container) {
    return container.ebean().builder().build();
  }

  @ExtraDb
  @Bean
  Database extraDb(PostgresContainer container) {
    return container.ebean().extraDatabaseBuilder()
      .name("extra")
      .initSqlFile("init-extra-database.sql")
      .seedSqlFile("seed-extra-database.sql")
      .build();
  }
}
```

- Use custom qualifiers (e.g., `@MainDb`, `@ReportingDb`) to inject the correct Database in your tests.

---

## 4. Example Test with Multiple Databases

```java
@InjectTest
class MultiDbTest {

  @Inject @MainDb Database mainDb;
  @Inject @ReportingDb Database reportingDb;
  @Inject TestEntityBuilder builder;

  @Test
  void testAcrossDatabases() {
    // Use builder with a specific database if needed
    User user = builder.save(User.class); // uses default injected db
    assertNotNull(mainDb.find(User.class, user.getId()));
    // ... test logic for reportingDb as well
  }
}
```

---

## 5. Best Practices & Troubleshooting

- Always use `@TestScope` and `@Factory` for test bean setup.
- Clean up test data if needed (TestEntityBuilder can help).
- Use unique database/container names to avoid conflicts in parallel test runs.
- For advanced container config, see [Testcontainers](https://www.testcontainers.org/) docs.
- If a bean isn’t injected, check for missing qualifiers or bean definitions.

---

For more, see:
- [Ebean TestEntityBuilder guide](https://github.com/ebean-orm/ebean/blob/HEAD/docs/guides/testing-with-testentitybuilder.md)
- [inject-test module](../../inject-test/src/test/java/)
- [avaje.io/inject](https://avaje.io/inject/)
