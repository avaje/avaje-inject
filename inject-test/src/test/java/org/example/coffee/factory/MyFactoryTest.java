package org.example.coffee.factory;

import io.avaje.inject.BeanContext;
import io.avaje.inject.SystemContext;
import org.example.coffee.parent.DesEngi;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MyFactoryTest {

  @Test
  public void methodsCalled() {
    try (BeanContext context = BeanContext.newBuilder().build()) {
      final MyFactory myFactory = context.getBean(MyFactory.class);
      assertThat(myFactory.methodsCalled()).contains("|useCFact", "|anotherCFact", "|buildEngi");
    }
  }

  @Test
  public void factoryMethod_createsConcreteImplementation() {
    DesEngi buildDesi = SystemContext.getBean(DesEngi.class, "BuildDesi1");
    assertThat(buildDesi.ignite()).isEqualTo("buildEngi1");

    DesEngi buildDesi2 = SystemContext.getBean(DesEngi.class, "BuildDesi2");
    assertThat(buildDesi2.ignite()).isEqualTo("MyEngi");
  }
}
