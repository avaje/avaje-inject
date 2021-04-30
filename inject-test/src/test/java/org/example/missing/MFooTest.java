package org.example.missing;

import io.avaje.inject.SystemContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MFooTest {

  @Test
  void test() {

    MFooUser bean = SystemContext.getBean(MFooUser.class);

    assertThat(bean.isUsePublicConstructor()).isTrue();
  }
}
