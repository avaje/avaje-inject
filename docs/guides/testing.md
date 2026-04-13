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

## Next Steps

- See [native image](native-image.md)
