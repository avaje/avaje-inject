package io.avaje.inject.generator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ScopeInfoGenericVarNameTest {

  @Test
  void single() {
    assertThat(ScopeInfo.genericVarName("org.example.ReqHolder<org.example.Req>")).isEqualTo("reqHolderReq");
  }

  @Test
  void multipleTypeArguments() {
    assertThat(ScopeInfo.genericVarName("java.util.Map<java.lang.String, java.lang.Long>")).isEqualTo("mapStringLong");
  }

  @Test
  void nested() {
    assertThat(ScopeInfo.genericVarName("java.util.Map<java.lang.String, java.util.List<a.A>>")).isEqualTo("mapStringListA");
  }

  @Test
  void wildcard() {
    assertThat(ScopeInfo.genericVarName("java.util.List<? extends java.lang.Number>")).isEqualTo("listNumber");
    assertThat(ScopeInfo.genericVarName("java.util.List<? super java.lang.Number>")).isEqualTo("listNumber");
    assertThat(ScopeInfo.genericVarName("java.util.List<?>")).isEqualTo("list");
  }

  @Test
  void innerClass() {
    assertThat(ScopeInfo.genericVarName("a.Outer.Inner<b.B>")).isEqualTo("innerB");
  }
}
