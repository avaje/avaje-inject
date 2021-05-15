package org.example.coffee.secondary;

import io.avaje.inject.ApplicationScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SecondaryEmailerTest {

  @Test
  public void wire() {

    SEmailer bean = ApplicationScope.get(SEmailer.class);
    assertThat(bean.email()).isEqualTo("other");
  }

}
