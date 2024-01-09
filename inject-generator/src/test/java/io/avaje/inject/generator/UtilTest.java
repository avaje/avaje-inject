package io.avaje.inject.generator;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilTest {

  @Test
  void notJavaLang() {
    assertFalse(Util.notJavaLang("java.lang.A"));
    assertFalse(Util.notJavaLang("java.lang.Foo"));

    assertTrue(Util.notJavaLang("not.lang.Foo"));
    assertTrue(Util.notJavaLang("java.lang.annotation.A"));
    assertTrue(Util.notJavaLang("java.lang.other.Foo"));
  }

  @Test
  void nestedShortName() {
    assertEquals(Util.shortName("com.example.Foo.Bar"), "Foo.Bar");
  }

  @Test
  void PackageOf() {
    ProcessingContext.testInit();
    assertEquals(Util.packageOf("com.example.Bar"), "com.example");
    assertEquals(Util.packageOf("com.example.other.Bar"), "com.example.other");
  }

  @Test
  void nestedPackageOf() {
    ProcessingContext.testInit();
    assertEquals(Util.nestedPackageOf("com.example.Foo.Bar"), "com.example");
    assertEquals(Util.nestedPackageOf("com.example.other.foo.Bar"), "com.example.other");
  }

  @Test
  void commonParent() {
    assertEquals(Util.commonParent(null, "org.b"), "org.b");
    assertEquals(Util.commonParent("org.a", null), "org.a");

    assertEquals(Util.commonParent("org.foo.web", "org.foo.web.other"), "org.foo.web");
    assertEquals(Util.commonParent("org.foo.web", "org.foo.web.other.more"), "org.foo.web");

    assertEquals(Util.commonParent("org.foo.web", "org.foo.service"), "org.foo");
    assertEquals(Util.commonParent("org.foo.web.foo", "org.foo.service.blah"), "org.foo");
  }

  @Test
  void isAspectProvider() {
    assertTrue(Util.isAspectProvider("io.avaje.inject.aop.AspectProvider<org.Foo>"));
    assertTrue(Util.isAspectProvider("io.avaje.inject.aop.AspectProvider<org.one.Bar"));
  }

  @Test
  void isAspectProvider_not() {
    assertFalse(Util.isAspectProvider("not.avaje.inject.aop.AspectProvider<org.Foo>"));
    assertFalse(Util.isAspectProvider("io.avaje.inject.not.AspectProvider<org.one.Bar"));
    assertFalse(Util.isAspectProvider("io.avaje.inject.aop.NotAspectProvider<org.one.Bar"));
    assertFalse(Util.isAspectProvider("io.avaje.inject.aop.NotAspectProvider"));
  }

  @Test
  void extractAspectType() {
    assertEquals(Util.extractAspectType("io.avaje.inject.aop.AspectProvider<org.Foo>"), "org.Foo");
    assertEquals(Util.extractAspectType("io.avaje.inject.aop.AspectProvider<org.one.Bar>"), "org.one.Bar");
  }

  @Test
  void unwrapProvider() {
    assertEquals(Util.unwrapProvider("jakarta.inject.Provider<org.Foo<com.Bazz>>"), "org.Foo<com.Bazz>");
  }

  @Test
  void extractMap() {
    assertEquals("Foo", Util.extractMap("java.util.Map<java.lang.String,? extends Foo>"));
    assertEquals("org.foo.Bar", Util.extractMap("java.util.Map<java.lang.String,? extends org.foo.Bar>"));
    assertEquals("org.foo.Bar", Util.extractMap("java.util.Map<java.lang.String,org.foo.Bar>"));
  }

  @Test
  void extractList() {
    assertEquals("Foo", Util.extractList("java.util.List<? extends Foo>"));
    assertEquals("org.foo.Bar", Util.extractList("java.util.List<? extends org.foo.Bar>"));
    assertEquals("org.foo.Bar", Util.extractList("java.util.List<org.foo.Bar>"));

  }

  @Test
  void extractSet() {
    assertEquals("Foo", Util.extractSet("java.util.Set<? extends Foo>"));
    assertEquals("org.foo.Bar", Util.extractSet("java.util.Set<? extends org.foo.Bar>"));
    assertEquals("org.foo.Bar", Util.extractSet("java.util.Set<org.foo.Bar>"));
  }

  @Test
  void addForInterface() {
    assertNull(Util.addForInterface("java.util.List<Some>"));
    assertEquals("Bar", Util.addForInterface("com.foo.Bar"));
  }

  @Test
  void validImportType() {
    assertTrue(Util.validImportType("my.Foo"));
    assertTrue(Util.validImportType("other.pack.Foo"));
  }

  @Test
  void trimmedAnnotation() {

  assertEquals(
        "java.util.Map<java.lang.String,java.lang.String>",
        Util.trimAnnotations(
            "java.util.@io.avaje.validation.constraints.NotEmpty(message=\"sus \", groups={io.avaje.validation.generator.models.valid.Ship.class}) Map<java.lang.@io.avaje.validation.constraints.NotEmpty(groups={io.avaje.validation.generator.models.valid.Ship.class}),@io.avaje.validation.constraints.NotBlank String,java.lang.@io.avaje.validation.constraints.NotBlank(groups={io.avaje.validation.generator.models.valid.Ship.class}),@io.avaje.validation.Valid String>"));

    assertEquals(
        "java.util.List<java.lang.String>",
        Util.trimAnnotations(
            "java.util.@jakarta.validation.constraints.NotEmpty(\"message(); ,\") List<java.lang.@jakarta.validation.constraints.NotNull String>"));
    assertEquals("int", Util.trimAnnotations("@jakarta.validation.constraints.Positive int"));
    assertEquals(
        "java.util.Map<java.lang.String,java.lang.String>",
        Util.trimAnnotations(
            "java.util.Map<@jakarta.validation.constraints.Positive(message=\"sus \", groups=1) java.lang.String,java.lang.String>"));

    }

  @Test
  void validImportType_not() {
    assertFalse(Util.validImportType("void"));
    assertFalse(Util.validImportType("Foo"));
    assertFalse(Util.validImportType("NoPackage"));
  }

  @Test
  void shortMethod() {
    assertThat(Util.shortMethod("foo.bar.processMe")).isEqualTo("bar.processMe");
  }

  @Test
  void initLower() {
    assertThat(Util.initLower("URL")).isEqualTo("url");
    assertThat(Util.initLower("URfoo")).isEqualTo("urfoo");
    assertThat(Util.initLower("Time")).isEqualTo("time");
    assertThat(Util.initLower("TiMe")).isEqualTo("tiMe");
  }

  @Test
  void trimGenerics() {
    assertThat(Util.trimGenerics("foo.bar.ProcessMe<java.lang.String>")).isEqualTo("foo.bar.ProcessMe");
    assertThat(Util.trimGenerics("foo.bar.ProcessMe")).isEqualTo("foo.bar.ProcessMe");
  }

  @Test
  void trimMethod_when_genericType() {
    assertThat(Util.trimMethod("foo.bar.ProcessMe<java.lang.String>")).isEqualTo("bar_ProcessMe");
  }

  @Test
  void sanitizeImports() {
    assertEquals("my.Foo", Util.sanitizeImports("my.Foo"));
    assertEquals("my.Foo", Util.sanitizeImports("my.Foo[]"));
    assertEquals("my.Foo", Util.sanitizeImports("@annotationMcgee my.Foo[]"));
    assertEquals("my.Foo", Util.sanitizeImports("@annotationMcgee my.Foo"));
    assertEquals("my.Foo", Util.sanitizeImports("@org.bar.annotationMcgee my.Foo[]"));
    assertEquals("my.Foo", Util.sanitizeImports("@org.bar.annotationMcgee my.Foo"));
    assertEquals("java.util.String", Util.sanitizeImports("java.util.String>"));
  }
}
