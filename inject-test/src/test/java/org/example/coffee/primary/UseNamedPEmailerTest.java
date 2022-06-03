package org.example.coffee.primary;

import io.avaje.inject.xtra.ApplicationScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UseNamedPEmailerTest {

  @Test
  void named_secondary() {
    UseNamedPEmailer bean = ApplicationScope.get(UseNamedPEmailer.class);
    assertThat(bean.emailNamed()).isEqualTo("other");
    assertThat(bean.emailOther()).isEqualTo("other");
    assertThat(bean.emailOtherPEmailer()).isEqualTo("primary");
  }
}
