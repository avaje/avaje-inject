package org.example.collectionorder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.avaje.inject.BeanScope;
import org.example.collectionorder.api.Contrib;
import org.example.collectionorder.api.SchemaConfig;
import org.junit.jupiter.api.Test;

class CollectionModuleOrderTest {

  @Test
  void moduleProvidedContrib() {
    try (BeanScope scope = BeanScope.builder().build()) {
      SchemaConfig config = scope.get(SchemaConfig.class);
      assertEquals(1, config.mapper.contribs.size());
    }
  }

  @Test
  void suppliedContrib_plusModuleProvided() {
    Contrib supplied = new Contrib() {};
    try (BeanScope scope = BeanScope.builder().bean(Contrib.class, supplied).build()) {
      SchemaConfig config = scope.get(SchemaConfig.class);
      assertEquals(2, config.mapper.contribs.size());
    }
  }
}
