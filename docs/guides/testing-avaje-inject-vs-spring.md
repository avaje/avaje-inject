# Avaje Inject vs Spring DI: Testing Setup Comparison

This guide is for developers familiar with Spring DI and testing. It shows how common Spring test patterns map to Avaje Inject, with code examples and notes on differences.

---

## 1. Basic Component Test: Side-by-Side

| Avaje Inject                | Spring Boot Test                |
|-----------------------------|---------------------------------|
| `@InjectTest`               | `@SpringBootTest`               |
| `@Inject`                   | `@Autowired`                    |
| `@Factory`/`@Bean` (test beans) | `@TestConfiguration`/`@Bean`  |
| Test beans are isolated     | Test beans may need `@Primary`  |
| No profiles needed          | Often uses `@ActiveProfiles`    |

**Avaje Inject Example:**
```java
@InjectTest
class MyServiceTest {
  @Inject MyService myService;
  @Test void testLogic() { /* ... */ }
}
```

**Spring Example:**
```java
@SpringBootTest
class MyServiceTest {
  @Autowired MyService myService;
  @Test void testLogic() { /* ... */ }
}
```

---

## 2. Test-Specific Beans

**Avaje Inject:**
```java
@TestScope
@Factory
class TestConfig {
  @Bean
  MyService myService() { return new MyService(...); }
}
```

**Spring:**
```java
@TestConfiguration
class SpringTestConfig {
  @Bean
  @Primary // needed if "main" bean also wired during test (not conditionally wired)
  MyService myService() { return new MyService(...); }
}
```

---

## 3. Profiles and Conditional Wiring

**Spring:**
- Use `@ConditionalOnMissingBean`, `@ConditionalOnProperty`, etc. for conditional wiring of "main" components to exclude those when testing.
- Alternatively use `@ActiveProfiles("test")` in test setup and `@Profile("!test")` on wiring of "main" components to exclude those when testing.

```java
@Profile("!test")
@Bean
DataSource prodDataSource() { ... }

@Profile("test")
@Bean
DataSource testDataSource() { ... }
```

**Avaje Inject:**
- Test beans wired via `@TestScope` automatically when the matching beans are not wired, so no need for profiles or conditional wiring.
- The test DI context is isolated from production beans (via layering of BeanScopes).

---

## 4. Summary Table: Key Differences

| Pattern/Need                | Spring DI                      | Avaje Inject                 |
|-----------------------------|-------------------------------|------------------------------|
| Test context annotation     | `@SpringBootTest`              | `@InjectTest`                |
| Inject beans                | `@Autowired`                   | `@Inject`                    |
| Test-only beans             | `@TestConfiguration` + `@Bean` | `@TestScope` + `@Factory`    |
| Override prod beans         | `@Primary` or `@Profile`       | Test beans override by scope |
| Conditional wiring          | `@Profile`, `@Conditional*`    | Not needed for tests         |
| Activate test config        | `@ActiveProfiles("test")`      | Not needed for tests         |

---

## 5. Notes for Spring Users
- Avaje Inject test beans in `@TestScope` automatically override production beans without needing `@Primary` or profiles.
- Avaje Inject test beans in `@TestScope` also have a global scope that is layered on top of the main BeanScope context, so they are isolated from production beans and won't accidentally interfere with them.
- No need for `@Primary`, `@Profile`, or `@ActiveProfiles` to control test wiring.
- No conditional wiring is needed for test beans—just define them in a `@TestScope @Factory`.
- Avaje Inject test context startup is typically fast using layering of BeanScopes so having *LOTS* of component testing is encouraged.

For more, see the [inject-test module](../../inject-test/src/test/java/) and [avaje.io/inject](https://avaje.io/inject/).
