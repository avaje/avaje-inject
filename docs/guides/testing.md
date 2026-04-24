# Testing with Avaje Inject

How to test beans and dependency injection with unit and component/integration tests.

## Unit Testing

Unit tests manually construct beans and use mocks for dependencies. This is fast, isolated,
and does not use the DI container.

**Example:**
```java
@Test
void testService() {
  UserRepository mockRepo = mock(UserRepository.class);
  UserService service = new UserService(mockRepo);
  when(mockRepo.findById(1)).thenReturn(new User(1, "John"));
  User user = service.findById(1);
  assertEquals("John", user.name);
}
```
**Best practices:**
- Use for pure logic, no DI needed.
- Mock only direct dependencies.

## Component/Integration Testing with @InjectTest

Component tests use the DI container to wire real beans. This is similar to Spring’s `@SpringBootTest`.

**Example:**
```java
@InjectTest
class UserServiceTest {
  @Inject UserService userService;

  @Test
  void findsUser() {
    User user = userService.findById(1);
    assertNotNull(user);
  }
}
```
**Best practices:**
- Use for service/business logic.
- Avoid mocking unless necessary for external systems.

## Providing Test Doubles (Mocks)

We can initialize `@Inject` fields with mocks to override real beans. This lets us control specific dependencies
and give them test specific behavior while using the real DI graph.

**Example:**
```java
@InjectTest
class ServiceWithMockTest {
  @Inject UserRepository userRepository = mock(UserRepository.class);
  @Inject UserService userService;

  @Test
  void testServiceWithMock() {
    when(userRepository.findById(1)).thenReturn(new User(1, "Jane"));
    User user = userService.findById(1);
    assertEquals("Jane", user.name);
  }
}
```

The DI container will use the initialized mock instead of creating a real `UserRepository` bean. This allows you to:
- Control external dependencies without manually constructing the service
- Leverage the full dependency graph while mocking specific beans
- Use mocks to invoke error conditions and simulate specific edge cases

## Using Mockito Annotations

Use `@Mock`/`@Spy` for cleaner setup. Mocks/spies are auto-wired into the test DI container.

**Example:**
```java
@InjectTest
class ServiceWithMockitoTest {
  @Mock UserRepository userRepository;
  @Spy Logger logger;
  @Inject UserService userService;

  @Test
  void testWithMockito() {
    when(userRepository.findById(1)).thenReturn(new User(1, "Alex"));
    User user = userService.findById(1);
    assertEquals("Alex", user.name);
    verify(userRepository).findById(1);
  }
}
```
**Annotations explained:**
- `@Mock` — Creates a complete mock that returns default values (null, empty collections, etc.)
- `@Spy` — Wraps a real object and allows selective method mocking while preserving real behavior

**Best practices:**
- Use `@Mock` for pure mocks, `@Spy` to partially mock real objects.

## Troubleshooting & Tips

- If a bean isn’t injected, check for missing `@Inject` or `@InjectTest`.
- Use try-with-resources for manual `TestScope` (advanced).

## More Examples

- See [inject-test module](../../inject-test/src/test/java/) for real-world tests.
- For advanced scenarios (e.g., Postgres/Ebean, LocalStack), see dedicated guides: `testing-postgres-ebean.md`, `testing-localstack.md` (coming soon).

---

For more, see the [full library reference](../LIBRARY.md) and [avaje.io/inject](https://avaje.io/inject/).
