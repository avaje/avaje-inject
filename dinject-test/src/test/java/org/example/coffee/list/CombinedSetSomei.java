package org.example.coffee.list;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class CombinedSetSomei {

  private final Set<Somei> somes;

  @Inject
  public CombinedSetSomei(Set<Somei> somes) {
    this.somes = somes;
  }

  public List<String> lotsOfSomes() {
    return somes.stream()
      .map(Somei::some)
      .collect(Collectors.toList());
  }
}
