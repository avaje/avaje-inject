package org.example.myapp.other;

import io.avaje.inject.BeanScope;
import io.avaje.inject.spi.GenericType;
import org.example.myapp.generic.Generic;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigFactoryTest {

  final Type TYPE_MapStringString = new GenericType<Map<String, String>>(){}.type();
  final Type TYPE_MapStringLong = new GenericType<Map<String, Long>>(){}.type();

  @SuppressWarnings("unchecked")
  @Test
  void doStuff() {
    try (BeanScope beanScope = BeanScope.builder().build()) {

      var map1 = (Map<String,String>)beanScope.get(TYPE_MapStringString);
      var map2 = (Map<String,Long>)beanScope.get(TYPE_MapStringLong);

      assertThat(map1).containsKeys("a", "b", "count");
      assertThat(map2).containsKeys("one", "two");
    }
  }
}
