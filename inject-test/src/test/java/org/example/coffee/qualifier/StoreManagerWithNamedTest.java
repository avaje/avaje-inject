package org.example.coffee.qualifier;

import io.avaje.inject.BeanScope;
import org.example.autonamed.MyAutoB2;
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
      Map<String, SomeStore> stores = beanScope.map(SomeStore.class);

      SomeStore green = stores.get("green");
      assertThat(green).isSameAs(greenStore);
      SomeStore blue = stores.get("blue");
      assertThat(blue).isSameAs(blueStore);

      // a map with unnamed component
      Map<String, MyAutoB2> mapWithUnnamed = beanScope.map(MyAutoB2.class);
      assertThat(mapWithUnnamed).hasSize(1);
    }
  }
}
