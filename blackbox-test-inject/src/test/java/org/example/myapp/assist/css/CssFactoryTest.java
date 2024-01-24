package org.example.myapp.assist.css;

import io.avaje.inject.BeanScope;
import org.example.myapp.assist.CssFactory;
import org.example.myapp.assist.CssThing;
import org.example.myapp.assist.JsThing;
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

      var cssThing = testScope.get(CssThing.class);
      String two = cssThing.scan("two");
      assertThat(two).isEqualTo("scanWith|path=two | somethin=hi");
    }
  }

  @Test
  void namesAndQualifier() {
    try (BeanScope testScope = BeanScope.builder().build()) {
      JsThing jsThing = testScope.get(JsThing.class);

      assertThat(jsThing.nameScan("one")).isEqualTo("jsScanNamed|path=one | somethin=hi");
      assertThat(jsThing.qualScan("one")).isEqualTo("jsScanQual|path=one | somethin=hi");
    }
  }

}
