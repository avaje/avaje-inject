package org.example.myapp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

@InjectTest
class ListFactoryTest {

  @Inject ListService list;

  @Test
  void test() {

    assertThat(list.strings()).contains("test1", "test1", "test3", "arg0", "arg1");
    assertThat(list.args()).containsOnly("arg0", "arg1");
    assertThat(list.argsAsSet()).containsOnly("arg0", "arg1");

    List<ListFactory.Some> somes = list.somes();
    assertThat(somes).hasSize(4);

    var names = somes.stream()
      .map(ListFactory.Some::name)
      .collect(Collectors.toList());

    assertThat(names).contains("m1", "m2", "m3", "s1");
  }
}
