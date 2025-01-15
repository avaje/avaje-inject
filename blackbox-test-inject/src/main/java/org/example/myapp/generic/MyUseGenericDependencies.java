package org.example.myapp.generic;

import jakarta.inject.Singleton;

import java.util.Map;
import java.util.function.BiConsumer;

@Singleton
class MyUseGenericDependencies {

  private final Aldrich<String, String> aldrich;
  private final Map<String, MyConsumer> genericMap;
  private final BiConsumer<MyA, MyB> biConsumer;

  MyUseGenericDependencies(Aldrich<String, String> aldrich,
                           Map<String, MyConsumer> genericMap,
                           BiConsumer<MyA, MyB> biConsumer) {
    this.aldrich = aldrich;
    this.genericMap = genericMap;
    this.biConsumer = biConsumer;
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
}
