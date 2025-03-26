package org.example.myapp.config;

import io.avaje.inject.BeanScope;
import io.avaje.inject.test.TestBeanScope;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CFactoryTest {

  @Test
  void optionalWithCascadingInterfaces() {
    try (BeanScope scope = TestBeanScope.builder().build()) {
      C2Face base = scope.get(C2Face.class, "base");
      CFace cface = scope.get(CFace.class, "optional");
      C2Face c2face = scope.get(C2Face.class, "optional");
      assertThat(base.msg()).isEqualTo("base");
      assertThat(cface.msg()).isEqualTo("optional");
      assertThat(c2face.msg()).isEqualTo("optional");

      List<C2Face> list = scope.list(C2Face.class);
      assertThat(list).hasSize(2);
    }
  }
}
