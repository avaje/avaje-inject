package io.avaje.inject.generator;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MethodReaderTest {

  @Test
  void addPreDestroy() {
    assertThat(MethodReader.addPreDestroy("close")).isEqualTo("$bean::close");
    assertThat(MethodReader.addPreDestroy("foo")).isEqualTo("$bean::foo");
  }

  @Test
  void addPreDestroyNested() {
    assertThat(MethodReader.addPreDestroy("foo().bar()")).isEqualTo("() -> $bean.foo().bar()");
  }
}