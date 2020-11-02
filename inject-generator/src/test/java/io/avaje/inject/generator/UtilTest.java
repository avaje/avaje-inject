package io.avaje.inject.generator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UtilTest {

  @Test
  public void commonParent() {

    assertEquals(Util.commonParent(null, "org.b"), "org.b");
    assertEquals(Util.commonParent("org.a", null), "org.a");

    assertEquals(Util.commonParent("org.foo.web", "org.foo.web.other"), "org.foo.web");
    assertEquals(Util.commonParent("org.foo.web", "org.foo.web.other.more"), "org.foo.web");

    assertEquals(Util.commonParent("org.foo.web", "org.foo.service"), "org.foo");
    assertEquals(Util.commonParent("org.foo.web.foo", "org.foo.service.blah"), "org.foo");
  }

  @Test
  public void extractList() {
    assertEquals("Foo", Util.extractList("List<? extends Foo>"));
    assertEquals("org.foo.Bar", Util.extractList("List<? extends org.foo.Bar>"));
  }

  @Test
  public void extractSet() {
    assertEquals("Foo", Util.extractSet("Set<? extends Foo>"));
    assertEquals("org.foo.Bar", Util.extractSet("Set<? extends org.foo.Bar>"));
  }

  @Test
  public void addForInterface() {
    assertNull(Util.addForInterface("java.util.List<Some>"));
    assertEquals("Bar", Util.addForInterface("com.foo.Bar"));
  }

  @Test
  public void validImportType() {
    assertTrue(Util.validImportType("my.Foo"));
    assertTrue(Util.validImportType("other.pack.Foo"));
  }

  @Test
  public void validImportType_not() {
    assertFalse(Util.validImportType("void"));
    assertFalse(Util.validImportType("Foo"));
    assertFalse(Util.validImportType("NoPackage"));
  }
}
