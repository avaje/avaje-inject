package org.example.myapp.generic;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import jakarta.inject.Singleton;

@Singleton
class MyUseGenericDependencies {

  private final Aldrich<String, String> aldrich;
  private final Map<String, MyConsumer> genericMap;
  private final BiConsumer<MyA, MyB> biConsumer;
  private final List<Generic<?>> genList;

  MyUseGenericDependencies(
      Aldrich<String, String> aldrich,
      Map<String, MyConsumer> genericMap,
      BiConsumer<MyA, MyB> biConsumer,
      List<Generic<?>> genList) {
    this.aldrich = aldrich;
    this.genericMap = genericMap;
    this.biConsumer = biConsumer;
    this.genList = genList;
  }

  Aldrich<String, String> getAldrich() {
    return aldrich;
  }

  Map<String, MyConsumer> getGenericMap() {
    return genericMap;
  }

  BiConsumer<MyA, MyB> getBiConsumer() {
    return biConsumer;
  }

  public List<Generic<?>> getGenList() {
    return genList;
  }
}
