package org.example.injectextension;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;

@InjectTest(scopePerTest = true)
class ScopePerMethodTest {
  @Inject ƎNA ena;
  static ƎNA ayna;

  @Test
  void one() {
    assertThat(ena).isNotSameAs(ayna);
    ayna = ena;
  }

  @Test
  void two() {
    assertThat(ena).isNotSameAs(ayna);
    ayna = ena;
  }
}
