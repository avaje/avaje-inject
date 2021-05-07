package org.example.missing;

import io.avaje.inject.SystemContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SinglePublicConstructorTest {

  @Test
  void beanWithSinglePublicConstructor_expect_publicConstructorChosen() {

    MFooUser2 bean = SystemContext.getBean(MFooUser2.class);

    assertThat(bean.isUsePublicConstructor()).isTrue();
  }
}
