package org.example.coffee.factory;

import io.avaje.inject.ApplicationScope;
import io.avaje.inject.BeanScope;
import org.example.coffee.parent.DesEngi;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MyFactoryTest {

  @Test
  public void methodsCalled() {
    try (BeanScope context = BeanScope.newBuilder().build()) {
      final MyFactory myFactory = context.getBean(MyFactory.class);
      assertThat(myFactory.methodsCalled()).contains("|useCFact", "|anotherCFact", "|buildEngi");
    }
  }

  @Test
  public void factoryMethod_createsConcreteImplementation() {
    DesEngi buildDesi = ApplicationScope.get(DesEngi.class, "BuildDesi1");
    assertThat(buildDesi.ignite()).isEqualTo("buildEngi1");

    DesEngi buildDesi2 = ApplicationScope.get(DesEngi.class, "BuildDesi2");
    assertThat(buildDesi2.ignite()).isEqualTo("MyEngi");
  }

  @Test
  public void factoryMethod_objectInterface() {
    DFact dfact = ApplicationScope.get(DFact.class);
    assertThat(dfact).isNotNull();

    IDFact idfact = ApplicationScope.get(IDFact.class);
    assertThat(idfact).isSameAs(dfact);
  }
}
