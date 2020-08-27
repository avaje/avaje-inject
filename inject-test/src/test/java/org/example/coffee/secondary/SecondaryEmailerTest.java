package org.example.coffee.secondary;

import io.avaje.inject.SystemContext;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SecondaryEmailerTest {

  @Test
  public void wire() {

    SEmailer bean = SystemContext.getBean(SEmailer.class);
    assertThat(bean.email()).isEqualTo("other");
  }

}
