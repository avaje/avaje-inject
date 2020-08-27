package org.example.coffee.generic;

import io.avaje.inject.SystemContext;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MultiGenericConsumerTest {

  @Test
  public void find() {

    MultiGenericConsumer bean = SystemContext.getBean(MultiGenericConsumer.class);

    assertThat(bean.findAndDo(34L)).isEqualTo("found 34 stuff");
    assertThat(bean.mushString()).isNotNull();
  }

}
