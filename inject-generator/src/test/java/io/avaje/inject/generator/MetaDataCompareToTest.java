package io.avaje.inject.generator;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class MetaDataCompareToTest {

  MetaData foo = new MetaData("foo.Foo", null);
  MetaData fooEq = new MetaData("foo.Foo", null);
  MetaData fooA = new MetaData("foo.Foo", "a");
  MetaData fooAEq = new MetaData("foo.Foo", "a");
  MetaData bar = new MetaData("b.Bar", null);
  MetaData fooProv = new MetaData("foo.Foo", null);
  MetaData fooProv2 = new MetaData("foo.Foo", null);
  MetaData fooProv3 = new MetaData("foo.Foo", null);

  MetaDataCompareToTest() {
    fooProv.setProvides(List.of("bar.Bar"));
    fooProv2.setProvides(List.of("bar.Bar"));
    fooProv3.setProvides(List.of("bar.Bar", "b.B2"));
  }

  @Test
  void compareTo_equals() {
    assertThat(foo.compareTo(fooEq)).describedAs("type only").isEqualTo(0);
    assertThat(fooA.compareTo(fooAEq)).describedAs("type and name").isEqualTo(0);
    assertThat(fooProv.compareTo(fooProv2)).describedAs("type and name and provides").isEqualTo(0);
  }

  @Test
  void compareTo_differentType() {
    assertEquals(4, foo.compareTo(bar));
    assertEquals(-4, bar.compareTo(foo));
  }

  @Test
  void compareTo_differentName() {
    assertEquals(-1, foo.compareTo(fooA));
    assertEquals(1, fooA.compareTo(foo));
  }

  @Test
  void compareTo_differentProvides() {
    assertEquals(5, fooProv.compareTo(foo));
    assertEquals(-5, foo.compareTo(fooProv));
    assertEquals(49, fooProv.compareTo(fooProv3));
    assertEquals(-49, fooProv3.compareTo(fooProv));
  }
}
