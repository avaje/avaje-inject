package org.example.coffee.qualifier;

import io.avaje.inject.BeanScope;
import org.example.autonamed.MyAutoB2;
import org.example.coffee.BeanScopeBuilderAddTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StoreManagerWithNamedTest {

  @Test
  void test() {
    try (BeanScope beanScope = BeanScope.builder().build()) {
      StoreManagerWithNamed manager = beanScope.get(StoreManagerWithNamed.class);
      String store = manager.store();
      assertThat(store).isEqualTo("blue");

      SomeStore greenStore = beanScope.get(SomeStore.class, "green");
      SomeStore blueStore = beanScope.get(SomeStore.class, "blue");
      SomeStore unNamedStore = beanScope.get(SomeStore.class);
      assertThat(blueStore.store()).isEqualTo("blue");
      assertThat(greenStore.store()).isEqualTo("green");
      assertThat(unNamedStore.store()).isEqualTo("noName");
      assertThat(greenStore).isInstanceOf(GreenStore.class);
      assertThat(blueStore).isInstanceOf(BlueStore.class);
      assertThat(unNamedStore).isInstanceOf(NoNameStore.class);

      Map<String, SomeStore> stores = beanScope.map(SomeStore.class);

      SomeStore green = stores.get("Green");
      assertThat(green).isSameAs(greenStore);
      SomeStore blue = stores.get("Blue");
      assertThat(blue).isSameAs(blueStore);

      // a map with unnamed component
      Map<String, MyAutoB2> mapWithUnnamed = beanScope.map(MyAutoB2.class);
      assertThat(mapWithUnnamed).hasSize(1);
    }
  }

  @Test
  void mapParent() {
    try (BeanScope parent = BeanScope.builder()
      .bean(SomeStore.class, new LocalStore())
      .modules(new BeanScopeBuilderAddTest.SillyModule())
      .build()) {

      try (BeanScope beanScope = BeanScope.builder().parent(parent).build()) {
        Map<String, SomeStore> stores = beanScope.map(SomeStore.class);
        assertThat(stores).hasSize(4);
      }
    }
  }

  static final class LocalStore implements SomeStore {
    @Override
    public String store() {
      return "foo";
    }
  }
}
