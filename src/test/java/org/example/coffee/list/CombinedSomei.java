package org.example.coffee.list;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.StringJoiner;

@Singleton
public class CombinedSomei {

  private final List<Somei> somes;

  @Inject
  public CombinedSomei(List<Somei> somes) {
    this.somes = somes;
  }

  public String lotsOfSomes() {
    StringJoiner join = new StringJoiner(",");
    for (Somei some : somes) {
      join.add(some.some());
    }
    return join.toString();
  }
}
