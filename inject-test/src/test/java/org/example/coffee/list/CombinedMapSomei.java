package org.example.coffee.list;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class CombinedMapSomei {

  private final Map<String, Somei> somes;

  /**
   * Inject map of beans keyed by qualifier name.
   */
  @Inject
  public CombinedMapSomei(Map<String, Somei> somes) {
    this.somes = somes;
  }

  public List<String> someKeys() {
    return new ArrayList<>(somes.keySet());
  }

  public List<String> someVals() {
    return somes.values().stream()
      .map(Somei::some)
      .collect(Collectors.toList());
  }
}
