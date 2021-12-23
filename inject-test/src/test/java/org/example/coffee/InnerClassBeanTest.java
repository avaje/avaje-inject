package org.example.coffee;

import io.avaje.inject.BeanScope;
import org.example.coffee.factory.BFact;
import org.example.coffee.factory.BeanWithGenericInterface;
import org.example.coffee.inner.InnerClassBean;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InnerClassBeanTest {

  @Test
  void test() {
    try (BeanScope context = BeanScope.newBuilder().build()) {
      InnerClassBean.MyBean bean = context.get(InnerClassBean.MyBean.class);
      assertThat(bean).isNotNull();
    }
  }
}
