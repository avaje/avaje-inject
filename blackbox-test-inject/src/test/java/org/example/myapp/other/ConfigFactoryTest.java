package org.example.myapp.other;

import io.avaje.inject.BeanScope;
import io.avaje.inject.spi.GenericType;
import org.example.myapp.generic.Generic;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigFactoryTest {

  final Type TYPE_MapStringString = new GenericType<Map<String, String>>(){};
  final Type TYPE_MapStringLong = new GenericType<Map<String, Long>>(){};
  final Type TYPE_SetLong = new GenericType<Set<Long>>(){};
  final Type TYPE_SetUUID = new GenericType<Set<UUID>>(){};

  @SuppressWarnings("unchecked")
  @Test
  void doStuff() {
    try (BeanScope beanScope = BeanScope.builder().build()) {

      var map1 = (Map<String,String>)beanScope.get(TYPE_MapStringString);
      var map2 = (Map<String,Long>)beanScope.get(TYPE_MapStringLong);
      assertThat(map1).containsKeys("a", "b", "count");
      assertThat(map2).containsKeys("one", "two");

      var setOfLong = (Set<Long>)beanScope.get(TYPE_SetLong);
      var setOfUUID = (Set<UUID>)beanScope.get(TYPE_SetUUID);
      assertThat(setOfLong).contains(1L, 2L);
      assertThat(setOfUUID).hasSize(1);
    }
  }
}
