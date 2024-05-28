package org.example.myapp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;

@InjectTest
class ListFactoryTest {

  @Inject ListService list;

  @Test
  void test() {

    assertThat(list.strings()).contains("test1", "test1", "test3");
  }
}
