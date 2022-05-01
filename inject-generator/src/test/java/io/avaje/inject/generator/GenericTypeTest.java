package io.avaje.inject.generator;

import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class GenericTypeTest {

  @Test
  void isGeneric() {
    assertFalse(GenericType.isGeneric("java.lang.List"));
    assertTrue(GenericType.isGeneric("java.lang.List<Foo>"));
    assertTrue(GenericType.isGeneric("java.lang.List<?>"));
  }

  @Test
  void maybe() {
    assertThat(GenericType.maybe("java.lang.List<Foo>").getMainType()).isEqualTo("java.lang.List");
    assertThat(GenericType.maybe("java.lang.List")).isNull();
  }

  @Test
  void parse() {
    GenericType type = new GenericTypeParser("my.exa.Repo<T,K>").parse();

    assertThat(type.getMainType()).isEqualTo("my.exa.Repo");
    assertThat(type.getParams()).hasSize(2);
    assertThat(type.getParams().get(0).getMainType()).isEqualTo("T");
    assertThat(type.getParams().get(1).getMainType()).isEqualTo("K");
  }

  @Test
  void parse_basic() {
    GenericType type = GenericType.parse("my.exa.Repo");
    assertThat(type.shortName()).isEqualTo("Repo");
    assertThat(type.topType()).isEqualTo("my.exa.Repo");
    assertThat(type.getMainType()).isNull();
    assertThat(type.getParams()).isEmpty();

    Set<String> importSet = new HashSet<>();
    type.addImports(importSet);
    assertThat(importSet).hasSize(1);
    assertThat(importSet).containsOnly("my.exa.Repo");
  }

  @Test
  void parse_genericWildcard() {
    GenericType type = GenericType.parse("my.exa.Repo<?>");
    assertThat(type.shortName()).isEqualTo("Repo");
    assertThat(type.topType()).isEqualTo("my.exa.Repo");
    assertThat(type.getMainType()).isNull();
    assertThat(type.getParams()).isEmpty();

    Set<String> importSet = new HashSet<>();
    type.addImports(importSet);
    assertThat(importSet).hasSize(1);
    assertThat(importSet).containsOnly("my.exa.Repo");
  }

  @Test
  void parse_withParam() {
    GenericType type = GenericType.parse("my.exa.Repo<my.Other>");
    assertThat(type.shortName()).isEqualTo("RepoOther");
    assertThat(type.topType()).isEqualTo("my.exa.Repo");
    assertThat(type.getMainType()).isEqualTo("my.exa.Repo");
    assertThat(type.getParams()).hasSize(1);
    assertThat(type.getParams().get(0).getMainType()).isEqualTo("my.Other");

    Set<String> importSet = new HashSet<>();
    type.addImports(importSet);
    assertThat(importSet).hasSize(2);
    assertThat(importSet).contains("my.exa.Repo", "my.Other");
  }

  @Test
  void parse_withParams() {
    GenericType type = GenericType.parse("my.exa.Repo<my.d.Haz,java.lang.Long>");

    assertThat(type.getMainType()).isEqualTo("my.exa.Repo");
    assertThat(type.getParams()).hasSize(2);
    assertThat(type.getParams().get(0).getMainType()).isEqualTo("my.d.Haz");
    assertThat(type.getParams().get(1).getMainType()).isEqualTo("java.lang.Long");
  }

  @Test
  void parse_withParams2() {
    GenericType type = GenericType.parse("java.util.concurrent.ConcurrentMap<java.lang.String,my.d.Has>");

    assertThat(type.getMainType()).isEqualTo("java.util.concurrent.ConcurrentMap");
    assertThat(type.getParams()).hasSize(2);
    assertThat(type.getParams().get(0).getMainType()).isEqualTo("java.lang.String");
    assertThat(type.getParams().get(1).getMainType()).isEqualTo("my.d.Has");

    LinkedHashSet<String> importTypes = new LinkedHashSet<>();
    type.addImports(importTypes);

    assertThat(importTypes).hasSize(2);
    assertThat(importTypes).containsExactly("java.util.concurrent.ConcurrentMap", "my.d.Has");
  }

  @Test
  void parse_withExtendsParams() {
    GenericType type = new GenericTypeParser("my.exa.Repo<? extends my.d.Haz,java.lang.Long>").parse();

    assertThat(type.getMainType()).isEqualTo("my.exa.Repo");
    assertThat(type.getParams()).hasSize(2);
    assertThat(type.getParams().get(0).getMainType()).isEqualTo("? extends my.d.Haz");
    assertThat(type.getParams().get(1).getMainType()).isEqualTo("java.lang.Long");
  }

  @Test
  void parse_withNestedParams() {
    GenericType type = new GenericTypeParser("my.exa.Repo<my.a.Prov<my.b.Haz>,my.a.Key<java.util.UUID>>").parse();

    assertThat(type.getMainType()).isEqualTo("my.exa.Repo");
    assertThat(type.getParams()).hasSize(2);
    assertThat(type.getParams().get(0).getMainType()).isEqualTo("my.a.Prov");
    assertThat(type.getParams().get(0).getParams()).hasSize(1);
    assertThat(type.getParams().get(0).getParams().get(0).getMainType()).isEqualTo("my.b.Haz");

    assertThat(type.getParams().get(1).getMainType()).isEqualTo("my.a.Key");
    assertThat(type.getParams().get(1).getParams()).hasSize(1);
    assertThat(type.getParams().get(1).getParams().get(0).getMainType()).isEqualTo("java.util.UUID");
  }

  @Test
  void shortName() {
    assertThat(GenericType.parse("java.lang.List<Foo>").shortName()).isEqualTo("ListFoo");
    assertThat(GenericType.parse("java.lang.List<org.Foo<com.Bar>>").shortName()).isEqualTo("ListFooBar");
  }

  @Test
  void hasParameter() {
    assertThat(GenericType.parse("java.lang.List<T>").hasParameter()).isTrue();
    assertThat(GenericType.parse("java.lang.List<org.Foo<TYPE>").hasParameter()).isTrue();
    assertThat(GenericType.parse("java.lang.List<org.Foo<com.Bar>>").hasParameter()).isFalse();
  }

  @Test
  void removeParameter() {
    assertThat(GenericType.removeParameter("java.lang.List<T>")).isEqualTo("java.lang.List");
    assertThat(GenericType.removeParameter("java.lang.List<org.Foo<T>>")).isEqualTo("java.lang.List");
    assertThat(GenericType.removeParameter("java.lang.List<org.Foo<TYPE>>")).isEqualTo("java.lang.List");
    assertThat(GenericType.removeParameter("face.FaceParam<T>")).isEqualTo("face.FaceParam");
    assertThat(GenericType.removeParameter("java.lang.List<org.Foo<com.Bar>>")).isEqualTo("java.lang.List<org.Foo<com.Bar>>");
  }

  @Test
  void trimWildcard() {
    assertEquals("my.exa.Repo", GenericType.trimWildcard("my.exa.Repo<?>"));
    assertEquals("my.exa.Repo", GenericType.trimWildcard("my.exa.Repo"));
    assertEquals("my.exa.Repo<my.Other>", GenericType.trimWildcard("my.exa.Repo<my.Other>"));
  }

  @Test
  void write() {
    GenericType type = new GenericTypeParser("my.exa.Repo<my.a.Prov<my.b.Haz>,my.a.Key<java.util.UUID>>").parse();

    StringWriter stringWriter = new StringWriter();
    Append append = new Append(stringWriter);
    type.writeShort(append);

    assertThat(stringWriter.toString()).isEqualTo("Repo<Prov<Haz>,Key<UUID>>");
  }
}
