package io.avaje.inject.spi;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

class DBeanScopeTest {

  @Test
  void combine_asFirst() {
    final List<Object> result = DBeanScope.combine(list("A", "B"), emptyList());
    assertThat(result).containsExactly("A", "B");
  }

  @Test
  void combine_asLast() {
    final List<Object> result = DBeanScope.combine(emptyList(), list("A", "B"));
    assertThat(result).containsExactly("A", "B");
  }
  @Test
  void combine_both() {
    final List<Object> result = DBeanScope.combine(list("A", "B"),list("C"));
    assertThat(result).containsExactly("A", "B", "C");
  }

  @Test
  void combine_bothImmutable() {
    final List<Object> result = DBeanScope.combine(List.<Object>of("A", "B"), List.<Object>of("C"));
    assertThat(result).containsExactly("A", "B", "C");
  }

  List<Object> list(String... vals) {
    return Arrays.asList(vals);
  }
}
