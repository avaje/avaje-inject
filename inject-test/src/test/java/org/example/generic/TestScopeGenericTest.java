package org.example.generic;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;

@InjectTest
public class TestScopeGenericTest {

  @Inject public GenericBean bean1;
  @Inject public GenericBean2 bean2;

  @Test
  void getTypeName() {
    assertThat(bean1).isNotNull();
    assertThat(bean2).isNotNull();
  }
}
