package org.example.coffee.list;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class CombinedSomei {

  private final List<Somei> somes;

  @Inject
  public CombinedSomei(List<Somei> somes) {
    this.somes = somes;
  }

  public List<String> lotsOfSomes() {
    return somes.stream()
      .map(Somei::some)
      .collect(Collectors.toList());
  }
}
