package org.example.myapp.config;

import io.avaje.inject.BeanScope;
import io.avaje.inject.test.TestBeanScope;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class BFactoryTest {

  @Test
  void beanMethodsOfSameType() {
    try (BeanScope scope = TestBeanScope.builder().build()) {
      BFace one = scope.get(BFace.class, "one");
      BFace two = scope.get(BFace.class, "two");
      BFace three = scope.get(BFace.class, "three");
      assertThat(one).isNotNull();
      assertThat(two).isNotNull();
      assertThat(three).isNotNull();

      List<BFace> list = scope.list(BFace.class);
      assertThat(list).hasSize(3);

      List<String> hi = list.stream().map(BFace::hi).collect(Collectors.toList());
      assertThat(hi).containsOnly("one", "two", "three");
    }
  }
}
