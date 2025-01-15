package org.example.coffee.factory;

import io.avaje.inject.xtra.ApplicationScope;
import io.avaje.inject.BeanScope;
import org.example.coffee.parent.DesEngi;
import org.example.request.MyHttpClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MyFactoryTest {

  @Test
  public void methodsCalled() {
    try (BeanScope context = BeanScope.builder().build()) {
      final MyFactory myFactory = context.get(MyFactory.class);
      assertThat(myFactory.methodsCalled()).contains("|useCFact", "|anotherCFact", "|buildEngi");
    }
  }

  @Test
  public void closableBeans_expect_closedViaPreDestroy() {

    final MyFactory.MyClose myClose;
    final MyFactory.MyAutoClose myAutoClose;
    final MyFactory.MyHttpClientGenerated myHttpClientGenerated;
    try (BeanScope context = BeanScope.builder().build()) {
      myClose = context.get(MyFactory.MyClose.class);
      myAutoClose = context.get(MyFactory.MyAutoClose.class);
      myHttpClientGenerated = (MyFactory.MyHttpClientGenerated) context.get(MyHttpClient.class);
    }
    assertThat(myClose.isClosed()).isTrue();
    assertThat(myAutoClose.isClosed()).isTrue();
    assertThat(myHttpClientGenerated.closed).isTrue();
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
