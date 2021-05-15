package org.example.coffee.generic;

import io.avaje.inject.ApplicationScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MultiGenericConsumerTest {

  @Test
  public void find() {

    MultiGenericConsumer bean = ApplicationScope.get(MultiGenericConsumer.class);

    assertThat(bean.findAndDo(34L)).isEqualTo("found 34 stuff");
    assertThat(bean.mushString()).isNotNull();
  }

}
