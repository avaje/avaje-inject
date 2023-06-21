package io.avaje.inject.generator;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScopeInfoTest {

  @Test
  void trimGenerics() {
    assertThat(ScopeInfo.trimGenerics("com.foo.Bar")).isEqualTo("com.foo.Bar");
    assertThat(ScopeInfo.trimGenerics("com.foo.Bar<String>")).isEqualTo("com.foo.Bar");
    assertThat(ScopeInfo.trimGenerics("com.foo.Bar<org.other.One>")).isEqualTo("com.foo.Bar");
  }
}
