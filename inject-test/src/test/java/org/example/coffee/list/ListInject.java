package org.example.coffee.list;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import jakarta.inject.Named;
import org.example.optional.Que;
import org.junit.jupiter.api.Test;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;

@InjectTest
class ListInject {

  @Inject List<Somei> testSomei;

  @Inject @Named("frodo") Optional<Que> frodo;

  @Test
  void test() {
    assertThat(testSomei).isNotEmpty();
    assertThat(frodo).isNotEmpty();
  }
}
