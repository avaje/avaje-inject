package org.example.coffee.generic;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.avaje.inject.xtra.ApplicationScope;

public class MultiGenericConsumerTest {

  @Test
  public void find() {

    MultiGenericConsumer bean = ApplicationScope.get(MultiGenericConsumer.class);

    assertThat(bean.findAndDo(34L)).isEqualTo("found 34 stuff");
    assertThat(bean.mushString()).isNotNull();
    assertThat(bean.list()).isNotEmpty();
  }
}
