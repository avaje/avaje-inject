# Testing with Avaje Inject

How to test beans and dependency injection.

## Unit Testing

Create beans manually for unit tests:

```java
@Test
public void testService() {
  UserRepository mockRepo = mock(UserRepository.class);
  UserService service = new UserService(mockRepo);

  when(mockRepo.findById(1)).thenReturn(new User(1, "John"));

  User user = service.findById(1);
  assertEquals("John", user.name);
}
```

## Integration Testing (also known as Component testing)

Use the actual container:

```java
@Test
@InjectTest
public class IntegrationTest {
  @Inject
  UserService userService;

  @Test
  public void testWithRealBeans() {
    User user = userService.findById(1);
    assertNotNull(user);
  }
}
```

## Providing Test Doubles (Mocks)

When a field annotated with `@Inject` has an initialized value, it's wired INTO the DI BeanScope as a test double:

```java
@InjectTest
public class ServiceWithMockTest {
  @Inject
  UserRepository userRepository = mock(UserRepository.class);

  @Inject
  UserService userService;

  @Test
  public void testServiceWithMock() {
    when(userRepository.findById(1)).thenReturn(new User(1, "Jane"));

    User user = userService.findById(1);
    assertEquals("Jane", user.name);
  }
}
```

The DI container will use the initialized mock instead of creating a real `UserRepository` bean. This allows you to:
- Control external dependencies without manually constructing the service
- Leverage the full dependency graph while mocking specific beans
- Keep integration test logic closer to production bean structure

## Using Mockito Annotations

Mockito provides `@Mock` and `@Spy` annotations for cleaner test setup:

```java
@InjectTest
public class ServiceWithMockitoTest {
  @Mock
  UserRepository userRepository;

  @Spy
  Logger logger;

  @Inject
  UserService userService;

  @Test
  public void testWithMockito() {
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

The mocks/spies are automatically wired into the DI container used for the test.

## Next Steps

- See [native image](native-image.md)

## Next Steps

- See [native image](native-image.md)
