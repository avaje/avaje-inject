package org.example.myapp.supplier;

import io.avaje.inject.BeanScope;
import org.example.external.aspect.sub.ExampleExternalAspectModule;
import org.example.myapp.MyappModule;
import org.junit.jupiter.api.Test;
import org.other.one.OneModule;

import static org.assertj.core.api.Assertions.assertThat;

class MySupFactoryTest {

  @Test
  void supplierOfFoo() {
    try (var beanScope = BeanScope.builder()
      .modules(new ExampleExternalAspectModule(), new OneModule(), new MyappModule())
      .build()) {

      MySupConsumer supConsumer = beanScope.get(MySupConsumer.class);
      MySupFactory.SupFoo current = supConsumer.current();
      assertThat(current.id()).isEqualTo(1);

      MySupFactory.SupFoo next = supConsumer.next();
      assertThat(next.id()).isEqualTo(2);

      MySupFactory.SupFoo next2 = supConsumer.next();
      assertThat(next2.id()).isEqualTo(3);
    }

  }
}
