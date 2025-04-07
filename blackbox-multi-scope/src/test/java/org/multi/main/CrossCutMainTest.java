package org.multi.main;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;
import org.multi.crosscut.BeanCross;
import org.multi.moda.BeanInModA;
import org.multi.modb.BeanInModB;

class CrossCutMainTest {

  @Test
  void buildScope() {
    try (BeanScope beanScope = CrossCutMain.buildScope()) {
      beanScope.get(BeanInModB.class);
      beanScope.get(BeanInModA.class);
      beanScope.get(BeanCross.class);
    }
  }
}
