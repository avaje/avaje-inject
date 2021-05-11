package org.example.optional;

import io.avaje.inject.SystemContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NoImpUserTest {

  @Test
  void injectNullable_withoutName_viaConstructor() {
    NoImpUser bean = SystemContext.getBean(NoImpUser.class);
    assertThat(bean.hasNoImplementation()).isTrue();
  }

  @Test
  void injectNullable_withoutName_viaFieldAndMethod() {
    NoImpUser2 bean = SystemContext.getBean(NoImpUser2.class);
    assertThat(bean.hasNoImplementationViaField()).isTrue();
    assertThat(bean.hasNoImplementationViaMethod()).isTrue();
  }

}
