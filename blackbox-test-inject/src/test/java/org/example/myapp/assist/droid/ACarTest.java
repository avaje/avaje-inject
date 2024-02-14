package org.example.myapp.assist.droid;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ACarTest {

  @Test
  void test() {
    try (BeanScope testScope = BeanScope.builder().build()) {
      ACarThing carThing = testScope.get(ACarThing.class);
      Paint p = new Paint() {};
      List<String> stringList = List.of("a");
      ACar aCar = carThing.doIt(p, 42, stringList);

      assertThat(aCar.paint).isSameAs(p);
      assertThat(aCar.type).isSameAs(stringList);
      assertThat(aCar.engine).isNotNull();
      assertThat(aCar.wheel).isNotNull();
      assertThat(aCar.radio).isNotNull();
    }
  }
}
