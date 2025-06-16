package org.example.myapp.config;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@InjectTest
class MyAopProviderConsumerTest {

  @Inject MyAopProviderConsumer consumer;

  @Test
  void doStuff() {
    AppConfig.Builder builder1 = consumer.doStuff();
    AppConfig.Builder builder2 = consumer.doStuff();

    assertThat(builder1).isNotNull();
    assertThat(builder2).isNotNull();
    assertThat(builder2).isNotSameAs(builder1);
  }
}
