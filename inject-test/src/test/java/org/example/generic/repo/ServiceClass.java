package org.example.generic.repo;

import jakarta.inject.Singleton;

import java.util.Map;

@Singleton
public class ServiceClass {

  final MapService<Map<String, Model1>> map1;
  final MapService<Map<String, Model2>> map2;

  public ServiceClass(MapService<Map<String, Model1>> map1, MapService<Map<String, Model2>> map2) {
    this.map1 = map1;
    this.map2 = map2;
  }

  public String process() {
    Map<String, Model1> m1 = map1.get();
    Map<String, Model2> m2 = map2.get();
    return m1.toString() + "|" + m2.toString();
  }
}
