package io.avaje.inject.test;

import io.avaje.inject.BeanScope;
import jakarta.inject.Inject;
import org.example.coffee.Pump;
import org.example.coffee.grind.AMusher;
import org.example.coffee.grind.Grinder;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.lang.reflect.Type;
import java.net.http.HttpClient;

import static org.assertj.core.api.Assertions.assertThat;

class MetaReaderTest {

  @Mock
  Pump pump;

  @Mock
  Grinder grinder;

  static @Mock AMusher musher;

  @Test
  void checkMetaReader() {
    MetaReader metaReader = new MetaReader(MetaReaderTest.class, null);
    assertThat(metaReader.hasInstanceInjection()).isTrue();
    assertThat(metaReader.hasClassInjection()).isTrue();
    assertThat(metaReader.mocks).hasSize(2);
    assertThat(metaReader.staticMocks).hasSize(1);
  }

  @Test
  void checkMetaReader_with_plugin() {
    MetaReader metaReader = new MetaReader(HelloBean.class, new MyPlugin());
    assertThat(metaReader.instancePlugin).isTrue();

    HelloBean helloBean = new HelloBean();
    metaReader.setFromScope(Mockito.mock(BeanScope.class), helloBean, true);

    assertThat(helloBean.client).isNotNull();
  }

  static class HelloBean {

    @Inject
    HttpClient client;
  }


  static class MyPlugin implements Plugin {

    public boolean forType(Type type) {
      return HttpClient.class.equals(type);
    }

    @Override
    public Scope createScope(BeanScope beanScope) {
      return new Scope();
    }

    static class Scope implements Plugin.Scope {

      HttpClient httpClient;

      @Override
      public Object create(Type type) {
        this.httpClient = HttpClient.newBuilder().build();
        return httpClient;
      }

      @Override
      public void close() {
        // do nothing
      }
    }
  }

}
