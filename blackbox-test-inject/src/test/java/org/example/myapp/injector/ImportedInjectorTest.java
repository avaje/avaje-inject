package org.example.myapp.injector;

import static org.assertj.core.api.Assertions.assertThat;

import org.example.myapp.injector.external.ExternalController;
import org.junit.jupiter.api.Test;

import io.avaje.inject.BeanScope;
import io.avaje.inject.spi.GenericType;
import io.avaje.inject.spi.Injector;

class ImportedInjectorTest {

  @Test
  void importedInjector_wiresExternallyCreatedInstance() {
    try (BeanScope scope = BeanScope.builder().build()) {
      ExternalController controller = new ExternalController();

      Injector<ExternalController> injector =
          scope.get(new GenericType<Injector<ExternalController>>() {});
      injector.inject(controller);

      assertThat(injector.getClass().getPackageName()).isEqualTo("org.example.myapp.injector");
      assertThat(controller.dep1).isNotNull();
      assertThat(controller.dep2()).isNotNull();
    }
  }
}
