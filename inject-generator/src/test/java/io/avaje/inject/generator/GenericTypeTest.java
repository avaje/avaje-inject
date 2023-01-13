package io.avaje.inject.generator;

import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class GenericTypeTest {

  @Test
  void equals() {
    GenericType t0 = GenericType.parse("java.lang.List");
    GenericType t1 = GenericType.parse("java.lang.List");
    assertThat(t0).isEqualTo(t1);

    GenericType t2 = GenericType.parse("java.lang.List<?>");
    assertThat(t0).isEqualTo(t2);
    GenericType t3 = GenericType.parse("java.lang.List<T>");
    assertThat(t0).isEqualTo(t3);

    Set<GenericType> set = new LinkedHashSet<>();
    set.add(t0);
    set.add(t1);
    set.add(t2);
    set.add(t3);
    assertThat(set).hasSize(1);
  }

  @Test
  void isGeneric() {
    assertFalse(GenericType.isGeneric("java.lang.List"));
    assertTrue(GenericType.isGeneric("java.lang.List<Foo>"));
    assertTrue(GenericType.isGeneric("java.lang.List<?>"));
    assertTrue(GenericType.isGeneric("java.lang.List<org.Foo>"));
  }


  @Test
  void parse() {
    GenericType type = new GenericTypeParser("my.exa.Repo<T,K>").parse();

    assertThat(type.mainType()).isEqualTo("my.exa.Repo");
    assertThat(type.params()).hasSize(2);
    assertThat(type.params().get(0).mainType()).isEqualTo("T");
    assertThat(type.params().get(1).mainType()).isEqualTo("K");
  }

  @Test
  void parse_basic() {
    GenericType type = GenericType.parse("my.exa.Repo");
    assertThat(type.shortName()).isEqualTo("Repo");
    assertThat(type.topType()).isEqualTo("my.exa.Repo");
    assertThat(type.mainType()).isNull();
    assertThat(type.params()).isEmpty();
    assertThat(type.isGenericType()).isFalse();
    assertThat(type.isProviderType()).isFalse();

    ImportTypeMap importSet = new ImportTypeMap();
    type.addImports(importSet);
    Set<String> forImport = importSet.forImport();
    assertThat(forImport).hasSize(1);
    assertThat(forImport).containsOnly("my.exa.Repo");
  }

  @Test
  void parse_provider() {
    GenericType type = GenericType.parse(Util.PROVIDER_PREFIX + "my.exa.Repo" + ">");
    assertThat(type.isGenericType()).isTrue();
    assertThat(type.isProviderType()).isTrue();
    assertThat(type.shortName()).isEqualTo("ProviderRepo");
  }

  @Test
  void parse_genericWildcard() {
    assertsForRepo(GenericType.parse("my.exa.Repo<?>"));
  }

  @Test
  void parse_genericTypeParam() {
    assertsForRepo(GenericType.parse("my.exa.Repo<Something>"));
  }

  @Test
  void parse_genericTypeParams() {
    assertsForRepo(GenericType.parse("my.exa.Repo<Something,T,X>"));
  }

  private void assertsForRepo(GenericType type) {
    assertThat(type.shortName()).isEqualTo("Repo");
    assertThat(type.topType()).isEqualTo("my.exa.Repo");
    assertThat(type.mainType()).isNull();
    assertThat(type.params()).isEmpty();
    assertThat(type.isGenericType()).isFalse();

    ImportTypeMap importSet = new ImportTypeMap();
    type.addImports(importSet);
    Set<String> forImport = importSet.forImport();
    assertThat(forImport).hasSize(1);
    assertThat(forImport).containsOnly("my.exa.Repo");
  }

  @Test
  void parse_withParam() {
    GenericType type = GenericType.parse("my.exa.Repo<my.Other>");
    assertThat(type.shortName()).isEqualTo("RepoOther");
    assertThat(type.topType()).isEqualTo("my.exa.Repo");
    assertThat(type.mainType()).isEqualTo("my.exa.Repo");
    assertThat(type.isGenericType()).isTrue();
    assertThat(type.params()).hasSize(1);
    assertThat(type.params().get(0).mainType()).isEqualTo("my.Other");

    ImportTypeMap importSet = new ImportTypeMap();
    type.addImports(importSet);
    Set<String> forImport = importSet.forImport();
    assertThat(forImport).hasSize(2);
    assertThat(forImport).contains("my.exa.Repo", "my.Other");
  }

  @Test
  void parse_withParams() {
    GenericType type = GenericType.parse("my.exa.Repo<my.d.Haz,java.lang.Long>");

    assertThat(type.mainType()).isEqualTo("my.exa.Repo");
    assertThat(type.isGenericType()).isTrue();
    assertThat(type.params()).hasSize(2);
    assertThat(type.params().get(0).mainType()).isEqualTo("my.d.Haz");
    assertThat(type.params().get(1).mainType()).isEqualTo("java.lang.Long");
  }

  @Test
  void parse_withParams2() {
    GenericType type = GenericType.parse("java.util.concurrent.ConcurrentMap<java.lang.String,my.d.Has>");

    assertThat(type.mainType()).isEqualTo("java.util.concurrent.ConcurrentMap");
    assertThat(type.isGenericType()).isTrue();
    assertThat(type.params()).hasSize(2);
    assertThat(type.params().get(0).mainType()).isEqualTo("java.lang.String");
    assertThat(type.params().get(1).mainType()).isEqualTo("my.d.Has");

    ImportTypeMap importSet = new ImportTypeMap();
    type.addImports(importSet);
    Set<String> forImport = importSet.forImport();
    assertThat(forImport).hasSize(2);
    assertThat(forImport).containsExactly("java.util.concurrent.ConcurrentMap", "my.d.Has");
  }

  @Test
  void parse_withExtendsParams() {
    GenericType type = new GenericTypeParser("my.exa.Repo<? extends my.d.Haz,java.lang.Long>").parse();

    assertThat(type.mainType()).isEqualTo("my.exa.Repo");
    assertThat(type.isGenericType()).isTrue();
    assertThat(type.params()).hasSize(2);
    assertThat(type.params().get(0).mainType()).isEqualTo("? extends my.d.Haz");
    assertThat(type.params().get(1).mainType()).isEqualTo("java.lang.Long");
  }

  @Test
  void parse_withNestedParams() {
    GenericType type = new GenericTypeParser("my.exa.Repo<my.a.Prov<my.b.Haz>,my.a.Key<java.util.UUID>>").parse();

    assertThat(type.mainType()).isEqualTo("my.exa.Repo");
    assertThat(type.isGenericType()).isTrue();
    assertThat(type.params()).hasSize(2);
    assertThat(type.params().get(0).mainType()).isEqualTo("my.a.Prov");
    assertThat(type.params().get(0).params()).hasSize(1);
    assertThat(type.params().get(0).params().get(0).mainType()).isEqualTo("my.b.Haz");

    assertThat(type.params().get(1).mainType()).isEqualTo("my.a.Key");
    assertThat(type.params().get(1).params()).hasSize(1);
    assertThat(type.params().get(1).params().get(0).mainType()).isEqualTo("java.util.UUID");
  }

  @Test
  void shortName() {
    assertThat(GenericType.parse("java.lang.List<T>").shortName()).isEqualTo("List");
    assertThat(GenericType.parse("java.lang.List<Foo>").shortName()).isEqualTo("List");
    assertThat(GenericType.parse("java.lang.List<FOO>").shortName()).isEqualTo("List");
    assertThat(GenericType.parse("java.lang.List<org.Foo<com.Bar>>").shortName()).isEqualTo("ListFooBar");
  }


  @Test
  void removeParameter() {
    assertThat(GenericType.removeParameter("java.lang.List<T>")).isEqualTo("java.lang.List");
    assertThat(GenericType.removeParameter("java.lang.List<org.Foo<T>>")).isEqualTo("java.lang.List");
    assertThat(GenericType.removeParameter("java.lang.List<org.Foo<TYPE>>")).isEqualTo("java.lang.List");
    assertThat(GenericType.removeParameter("face.FaceParam<T>")).isEqualTo("face.FaceParam");
    assertThat(GenericType.removeParameter("java.lang.List<org.Foo<com.Bar>>")).isEqualTo("java.lang.List");
  }

  @Test
  void trimWildcard() {
    assertEquals("my.exa.Repo", GenericType.trimWildcard("my.exa.Repo<?>"));
    assertEquals("my.exa.Repo", GenericType.trimWildcard("my.exa.Repo"));
    assertEquals("my.exa.Repo<my.Other>", GenericType.trimWildcard("my.exa.Repo<my.Other>"));
  }

  @Test
  void trimGenericParams() {
    assertEquals("my.exa.Repo", GenericType.trimGenericParams("my.exa.Repo<?>"));
    assertEquals("my.exa.Repo", GenericType.trimWildcard("my.exa.Repo<T>"));
    assertEquals("my.exa.Repo", GenericType.trimWildcard("my.exa.Repo<A,B>"));
    assertEquals("my.exa.Repo", GenericType.trimWildcard("my.exa.Repo<MyParam,OtherParam,More>"));
    assertEquals("my.exa.Repo<my.Other>", GenericType.trimWildcard("my.exa.Repo<my.Other>"));
    assertEquals("my.exa.Repo<unexpected", GenericType.trimWildcard("my.exa.Repo<unexpected"));
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
