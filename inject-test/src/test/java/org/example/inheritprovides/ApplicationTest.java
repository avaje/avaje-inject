package org.example.inheritprovides;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationTest {

  @Test
  void asd() {
    try (BeanScope beanScope =  BeanScope.builder().build()) {
      Application application = beanScope.get(Application.class);
      List<? extends Controller> controllers = application.getControllers();

      assertThat(application).isNotNull();
      assertThat(controllers).hasSize(2);
    }
  }
}
