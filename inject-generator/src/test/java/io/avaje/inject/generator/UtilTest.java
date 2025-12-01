package io.avaje.inject.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

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
  void commonParent() {
    assertEquals(Util.commonParent(null, "org.b"), "org.b");
    assertEquals(Util.commonParent("org.a", null), "org.a");

    assertEquals(Util.commonParent("org.foo.web", "org.foo.web.other"), "org.foo.web");
    assertEquals(Util.commonParent("org.foo.web", "org.foo.web.other.more"), "org.foo.web");

    assertEquals(Util.commonParent("org.foo.web", "org.foo.service"), "org.foo");
    assertEquals(Util.commonParent("org.foo.web.foo", "org.foo.service.blah"), "org.foo");
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
    assertTrue(Util.validImportType("java.lang.something.Foo", "org.foo"));
    assertTrue(Util.validImportType("org.foo.some.Bar", "org.foo"));
    assertTrue(Util.validImportType("org.other.Bar", "org.foo"));
  }

  @Test
  void validImportType_not() {
    assertFalse(Util.validImportType("int", "org.foo"));
    assertFalse(Util.validImportType("java.lang.Integer", "org.foo"));
    assertFalse(Util.validImportType("org.foo.Bar", "org.foo"));
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

  @Test
  void nestedShortName() {
    assertEquals("Foo.Bar", Util.shortName("com.example.Foo.Bar") );
    assertEquals("Flow.Publisher", Util.shortName("java.util.concurrent.Flow.Publisher"));
    assertEquals("Outer.Inner", Util.shortName("com.foo.Outer.Inner"));
    assertEquals("Only", Util.shortName("a.b.c.Only"));
    assertEquals("simple", Util.shortName("simple"));
    assertEquals("Math", Util.shortName("java.lang.Math"));
    assertEquals("BigDecimal", Util.shortName("java.math.BigDecimal"));
  }
}
