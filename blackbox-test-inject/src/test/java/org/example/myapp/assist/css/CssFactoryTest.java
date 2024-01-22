package org.example.myapp.assist.css;

import io.avaje.inject.BeanScope;
import org.example.myapp.assist.CssFactory;
import org.example.myapp.assist.Scanner;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CssFactoryTest {

  @Test
  void scanner() {
    try (BeanScope testScope = BeanScope.builder().build()) {
      CssFactory cssFactory = testScope.get(CssFactory.class);
      Scanner scanner = cssFactory.scanner("one");

      assertThat(scanner).isInstanceOf(CssScanner.class);

      String result = scanner.scan();
      assertThat(result).isEqualTo("scanWith|path=one | somethin=hi");
    }
  }

}
