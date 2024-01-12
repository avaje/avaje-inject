//package io.avaje.inject.generator;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import java.util.LinkedHashSet;
//import java.util.Set;
//
//import org.junit.jupiter.api.Test;
//
//public class UTypeTest {
//
//  @Test
//  void equals() {
//    UType t0 = StringTypeParser.parse("java.lang.List");
//    UType t1 = StringTypeParser.parse("java.lang.List");
//    assertThat(t0).isEqualTo(t1);
//
//    UType t2 = StringTypeParser.parse("java.lang.List<?>");
//    assertThat(t0).isEqualTo(t2);
//    UType t3 = StringTypeParser.parse("java.lang.List<T>");
//    assertThat(t0).isEqualTo(t3);
//
//    Set<UType> set = new LinkedHashSet<>();
//    set.add(t0);
//    set.add(t1);
//    set.add(t2);
//    set.add(t3);
//    assertThat(set).hasSize(1);
//  }
//
//  @Test
//  void parse() {
//    UType type = StringTypeParser.parse("my.exa.Repo<T,K>");
//
//    assertThat(type.mainType()).isEqualTo("my.exa.Repo");
//    assertThat(type.componentTypes()).hasSize(2);
//    assertThat(type.componentTypes().get(0).mainType()).isEqualTo("T");
//    assertThat(type.componentTypes().get(1).mainType()).isEqualTo("K");
//  }
//
//  @Test
//  void parse_basic() {
//    UType type = StringTypeParser.parse("my.exa.Repo");
//    assertThat(type.shortType()).isEqualTo("Repo");
//    assertThat(type.mainType()).isEqualTo("my.exa.Repo");
//    assertThat(type.mainType()).isNull();
//    assertThat(type.componentTypes()).isEmpty();
//    assertThat(type.isGeneric()).isFalse();
//    assertThat(Util.isProvider(type.mainType())).isFalse();
//
//    ImportTypeMap importSet = new ImportTypeMap();
//
//    Set<String> forImport = importSet.forImport();
//    assertThat(forImport).hasSize(1);
//    assertThat(forImport).containsOnly("my.exa.Repo");
//  }
//
//  @Test
//  void parse_provider() {
//    UType type = StringTypeParser.parse(Util.PROVIDER_PREFIX + "<my.exa.Repo>");
//    assertThat(type.isGeneric()).isTrue();
//    assertThat(Util.isProvider(type.mainType())).isTrue();
//    assertThat(Util.shortName(type)).isEqualTo("ProviderRepo");
//  }
//
//  @Test
//  void parse_intersection() {
//    UType type = StringTypeParser.parse(Util.PROVIDER_PREFIX + "<T extends Number & Runnable>");
//    assertThat(type.isGeneric()).isTrue();
//    assertThat(Util.isProvider(type.mainType())).isTrue();
//    assertThat(Util.shortName(type)).isEqualTo("ProviderRepo");
//  }
//
//  @Test
//  void parse_genericWildcard() {
//    assertsForRepo(StringTypeParser.parse("my.exa.Repo<?>"));
//  }
//
//  @Test
//  void parse_genericTypeParam() {
//    assertsForRepo(StringTypeParser.parse("my.exa.Repo<Something>"));
//  }
//
//  @Test
//  void parse_genericTypeParams() {
//    assertsForRepo(StringTypeParser.parse("my.exa.Repo<Something,T,X>"));
//  }
//
//  private void assertsForRepo(UType type) {
//    assertThat(Util.shortName(type)).isEqualTo("Repo");
//    assertThat(type.mainType()).isEqualTo("my.exa.Repo");
//    assertThat(type.componentTypes()).isNotEmpty();
//    assertThat(type.isGeneric()).isFalse();
//
//    ImportTypeMap importSet = new ImportTypeMap();
//
//    Set<String> forImport = importSet.forImport();
//    assertThat(forImport).hasSize(1);
//    assertThat(forImport).containsOnly("my.exa.Repo");
//  }
//
//  @Test
//  void parse_withParam() {
//    UType type = StringTypeParser.parse("my.exa.Repo<my.Other>");
//    assertThat(Util.shortName(type)).isEqualTo("RepoOther");
//    assertThat(type.mainType()).isEqualTo("my.exa.Repo");
//    assertThat(type.mainType()).isEqualTo("my.exa.Repo");
//    assertThat(type.isGeneric()).isTrue();
//    assertThat(type.componentTypes()).hasSize(1);
//    assertThat(type.componentTypes().get(0).mainType()).isEqualTo("my.Other");
//
//    ImportTypeMap importSet = new ImportTypeMap();
//
//    Set<String> forImport = importSet.forImport();
//    assertThat(forImport).hasSize(2);
//    assertThat(forImport).contains("my.exa.Repo", "my.Other");
//  }
//
//  @Test
//  void parse_withParams() {
//    UType type = StringTypeParser.parse("my.exa.Repo<my.d.Haz,java.lang.Long>");
//
//    assertThat(type.mainType()).isEqualTo("my.exa.Repo");
//    assertThat(type.isGeneric()).isTrue();
//    assertThat(type.componentTypes()).hasSize(2);
//    assertThat(type.componentTypes().get(0).mainType()).isEqualTo("my.d.Haz");
//    assertThat(type.componentTypes().get(1).mainType()).isEqualTo("java.lang.Long");
//  }
//
//  @Test
//  void parse_withParams2() {
//    UType type = StringTypeParser.parse("java.util.concurrent.ConcurrentMap<java.lang.String,my.d.Has>");
//
//    assertThat(type.mainType()).isEqualTo("java.util.concurrent.ConcurrentMap");
//    assertThat(type.isGeneric()).isTrue();
//    assertThat(type.componentTypes()).hasSize(2);
//    assertThat(type.componentTypes().get(0).mainType()).isEqualTo("java.lang.String");
//    assertThat(type.componentTypes().get(1).mainType()).isEqualTo("my.d.Has");
//
//    ImportTypeMap importSet = new ImportTypeMap();
//    importSet.addAll(type.importTypes());
//    Set<String> forImport = importSet.forImport();
//    assertThat(forImport).hasSize(2);
//    assertThat(forImport).containsExactly("java.util.concurrent.ConcurrentMap", "my.d.Has");
//  }
//
//  @Test
//  void parse_withExtendsParams() {
//    UType type = StringTypeParser.parse("my.exa.Repo<? extends my.d.Haz,java.lang.Long>");
//
//    assertThat(type.mainType()).isEqualTo("my.exa.Repo");
//    assertThat(type.isGeneric()).isTrue();
//    assertThat(type.componentTypes()).hasSize(2);
//    assertThat(type.componentTypes().get(0).mainType()).isEqualTo("? extends my.d.Haz");
//    assertThat(type.componentTypes().get(1).mainType()).isEqualTo("java.lang.Long");
//  }
//
//  @Test
//  void parse_withNestedParams() {
//    UType type = StringTypeParser.parse("my.exa.Repo<my.a.Prov<my.b.Haz>,my.a.Key<java.util.UUID>>");
//
//    assertThat(type.mainType()).isEqualTo("my.exa.Repo");
//    assertThat(type.isGeneric()).isTrue();
//    assertThat(type.componentTypes()).hasSize(2);
//    assertThat(type.componentTypes().get(0).mainType()).isEqualTo("my.a.Prov");
//    assertThat(type.componentTypes().get(0).componentTypes()).hasSize(1);
//    assertThat(type.componentTypes().get(0).componentTypes().get(0).mainType())
//        .isEqualTo("my.b.Haz");
//
//    assertThat(type.componentTypes().get(1).mainType()).isEqualTo("my.a.Key");
//    assertThat(type.componentTypes().get(1).componentTypes()).hasSize(1);
//    assertThat(type.componentTypes().get(1).componentTypes().get(0).mainType())
//        .isEqualTo("java.util.UUID");
//  }
//
//  @Test
//  void shortType() {
//    assertThat(Util.shortName(StringTypeParser.parse("java.lang.List<T>"))).isEqualTo("List");
//    assertThat(Util.shortName(StringTypeParser.parse("java.lang.List<Foo>"))).isEqualTo("List");
//    assertThat(Util.shortName(StringTypeParser.parse("java.lang.List<FOO>"))).isEqualTo("List");
//    assertThat(Util.shortName(StringTypeParser.parse("java.lang.List<org.Foo<com.Bar>>")))
//        .isEqualTo("ListFooBar");
//    assertThat(Util.shortName(StringTypeParser.parse("java.lang.List<org.Foo<? extends com.Bar>>")))
//        .isEqualTo("ListFooBar");
//  }
//
//  //
//  //  @Test
//  //  void removeParameter() {
//  //    assertThat(UType.removeParameter("java.lang.List<T>")).isEqualTo("java.lang.List");
//  //    assertThat(UType.removeParameter("java.lang.List<org.Foo<T>>")).isEqualTo("java.lang.List");
//  //
//  // assertThat(UType.removeParameter("java.lang.List<org.Foo<TYPE>>")).isEqualTo("java.lang.List");
//  //    assertThat(UType.removeParameter("face.FaceParam<T>")).isEqualTo("face.FaceParam");
//  //
//  // assertThat(UType.removeParameter("java.lang.List<org.Foo<com.Bar>>")).isEqualTo("java.lang.List");
//  //  }
//
//  //  @Test
//  //  void trimWildcard() {
//  //    assertEquals("my.exa.Repo", UType.trimWildcard("my.exa.Repo<?>"));
//  //    assertEquals("my.exa.Repo", UType.trimWildcard("my.exa.Repo"));
//  //    assertEquals("my.exa.Repo<my.Other>", UType.trimWildcard("my.exa.Repo<my.Other>"));
//  //  }
//  //
//  //  @Test
//  //  void trimGenericParams() {
//  //    assertEquals("my.exa.Repo", UType.trimGenericParams("my.exa.Repo<?>"));
//  //    assertEquals("my.exa.Repo", UType.trimWildcard("my.exa.Repo<T>"));
//  //    assertEquals("my.exa.Repo", UType.trimWildcard("my.exa.Repo<A,B>"));
//  //    assertEquals("my.exa.Repo", UType.trimWildcard("my.exa.Repo<MyParam,OtherParam,More>"));
//  //    assertEquals("my.exa.Repo<my.Other>", UType.trimWildcard("my.exa.Repo<my.Other>"));
//  //    assertEquals("my.exa.Repo<unexpected", UType.trimWildcard("my.exa.Repo<unexpected"));
//  //  }
//
//  @Test
//  void write() {
//    UType type = StringTypeParser.parse("my.exa.Repo<my.a.Prov<my.b.Haz>,my.a.Key<java.util.UUID>>");
//
//    assertThat(type.shortType()).isEqualTo("Repo<Prov<Haz>,Key<UUID>>");
//  }
//}
