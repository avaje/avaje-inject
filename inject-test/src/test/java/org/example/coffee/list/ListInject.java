package org.example.coffee.list;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;

@InjectTest
class ListInject {

  @Inject List<Somei> testSomei;

  @Test
  void test() {

    assertThat(testSomei).isNotEmpty();
  }
}
