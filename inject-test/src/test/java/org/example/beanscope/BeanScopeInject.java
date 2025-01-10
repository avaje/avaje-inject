package org.example.beanscope;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.avaje.inject.BeanScope;
import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;

@InjectTest
class BeanScopeInjectTest {

  @Inject BeanScope beanScope;

  @Test
  void test() {
    assertThat(beanScope.all()).isNotEmpty();
  }
}
