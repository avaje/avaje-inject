package org.example.myapp.assist.generic;

import static org.assertj.core.api.Assertions.assertThat;

import org.example.myapp.assist.generic.NightLord.Caligo;
import org.example.myapp.assist.generic.NightLord.Libra;
import org.junit.jupiter.api.Test;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;

@InjectTest
class AssistGenericTest {

  @Inject NightReign nightReign;

  @Test
  void test() {
    assertThat(nightReign.caligo()).isInstanceOf(Caligo.class);
    assertThat(nightReign.libra()).isInstanceOf(Libra.class);
  }
}
