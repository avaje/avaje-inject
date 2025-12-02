package io.example.myapp.pkg_private;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PackagePrivateTest {

  @Test
  void package_private_wires() {
    try (BeanScope beanScope = BeanScope.builder().build()) {
      var calculator = beanScope.get(Calculator.class);
      var adder = beanScope.get(Adder.class);

      assertThat(calculator).isNotNull();
      assertThat(adder).isNotNull();

      int result = calculator.sum(3, 7);
      assertThat(result).isEqualTo(10);
    }
  }
}
