package org.multi.crosscut;


import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;
import org.multi.modb.BeanInModB;

import static org.assertj.core.api.Assertions.assertThat;

class BeanCrossTest {

  @Test
  void bootstrap() {

    try (BeanScope beanScope = BeanScope.builder()
//      .modules(new CrossCutModule())
      .build()) {

//      var beanInModB = beanScope.get(BeanInModB.class);
//      assertThat(beanInModB).isNotNull();
    }

  }
}
