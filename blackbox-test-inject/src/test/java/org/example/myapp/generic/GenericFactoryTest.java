package org.example.myapp.generic;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;

@InjectTest
class GenericFactoryTest {

  @Inject Generic<Integer> intymcintface;
  @Inject Generic<String> stringy;
  @Inject MyUseGenericDependencies others;

  @Test
  void test() {
    assertThat(intymcintface).isNotNull();
    assertThat(stringy).isNotNull();
    assertThat(others.getAldrich()).isNotNull();
    assertThat(others.getGenericMap()).isNotNull();
    assertThat(others.getBiConsumer()).isNotNull();
    assertThat(others.getGenList()).hasSize(2);
    assertThat(others.getGenList()).contains(stringy, intymcintface);
  }
}
