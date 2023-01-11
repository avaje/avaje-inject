package io.avaje.inject.generator;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ImportTypeMapTest {

  @Test
  void add() {
    ImportTypeMap map = new ImportTypeMap();

    String sn = map.add("org.foo.Bax");
    assertThat(sn).isEqualTo("Bax");

    String sn1 = map.add("org.foo.Bazz");
    assertThat(sn1).isEqualTo("Bazz");

    String sn2 = map.add("org.other.Bax");
    assertThat(sn2).isEqualTo("org.other.Bax");

    Set<String> forImports = map.forImport();
    assertThat(forImports).hasSize(2);
    assertThat(forImports).containsExactly("org.foo.Bax", "org.foo.Bazz");
  }
}
