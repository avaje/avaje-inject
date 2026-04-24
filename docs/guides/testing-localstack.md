# Testing with LocalStack and Avaje Inject

This guide shows how to set up integration/component tests using Avaje Inject and LocalStack, with AWS SDK v2 clients (e.g., SqsClient). LocalStack provides a local AWS cloud stack for testing SQS, S3, DynamoDB, and more.

---

## 1. Test Configuration Example

Define a test configuration with `@TestScope` and `@Factory` to provide beans for LocalStack and SqsClient.

```java
@TestScope
@Factory
class TestConfig {

  @Bean
  LocalstackContainer localstack() {
    return LocalstackContainer.builder("4.3.0")
      // .mirror("<your-mirror-repo>") // optional: use a local/ECR mirror
      .awsRegion("ap-southeast-2")
      .services("sqs") // comma-separated list, e.g. "sqs,s3,dynamodb"
      .containerName("ut_localstack")
      .port(4567)
      .start();
  }

  @Bean
  SqsClient sqsClient(LocalstackContainer localstack) {
    return localstack.sdk2().sqsClient();
  }
}
```

- The LocalStack container is started automatically for tests.
- The SqsClient is configured to connect to the local SQS endpoint.
- Add more AWS services as needed via `.services()`.

---

## 2. Example Test Using SqsClient

```java
@InjectTest
class SqsServiceTest {

  @Inject SqsClient sqsClient;

  @Test
  void testSendAndReceive() {
    // Create a queue, send a message, receive it, etc.
    String queueUrl = sqsClient.createQueue(r -> r.queueName("test-queue")).queueUrl();
    sqsClient.sendMessage(r -> r.queueUrl(queueUrl).messageBody("hello world"));
    var messages = sqsClient.receiveMessage(r -> r.queueUrl(queueUrl)).messages();
    assertFalse(messages.isEmpty());
    assertEquals("hello world", messages.get(0).body());
  }
}
```

---

## 3. Best Practices & Troubleshooting

- Use `@TestScope` and `@Factory` for test bean setup.
- Use `.services()` to limit LocalStack startup to only the AWS services you need.
- Clean up resources (queues, buckets, etc.) after tests if needed.
- For advanced config, see [LocalStack docs](https://docs.localstack.cloud/) and [Testcontainers LocalStack module](https://www.testcontainers.org/modules/localstack/).
- If a bean isn’t injected, check for missing bean definitions or incorrect service names.

---

For more, see:
- [inject-test module](../../inject-test/src/test/java/)
- [avaje.io/inject](https://avaje.io/inject/)
- [LocalStack](https://localstack.cloud/)
