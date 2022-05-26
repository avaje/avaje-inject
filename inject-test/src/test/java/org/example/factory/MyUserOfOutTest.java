package org.example.factory;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MyUserOfOutTest {

  @Test
  void factoryBeanWiredByInheritedInterface() {
    try (BeanScope beanScope = BeanScope.builder().build()) {

      MyUserOfOut myUserOfOut = beanScope.get(MyUserOfOut.class);
      assertThat(myUserOfOut).isNotNull();

      OutIFace outIFace = beanScope.get(OutIFace.class);
      assertThat(outIFace).isSameAs(myUserOfOut.source);
    }
  }
}
