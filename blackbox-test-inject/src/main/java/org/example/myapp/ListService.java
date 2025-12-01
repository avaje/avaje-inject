package org.example.myapp;

import java.util.List;
import java.util.Set;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
public class ListService {

  private final List<String> strings;
  private final List<String> argStrings;
  private final Set<String> argsAsSet;
  private final List<ListFactory.Some> somes;

  public ListService(
      List<String> allStrings,
      @Named("args") List<String> argStrings,
      @Named("args") Set<String> argsAsSet,
      List<ListFactory.Some> somes) {

    this.strings = allStrings;
    this.argStrings = argStrings;
    this.argsAsSet = argsAsSet;
    this.somes = somes;
  }

  public List<String> args() {
    return argStrings;
  }

  public Set<String> argsAsSet() {
    return argsAsSet;
  }

  public List<String> strings() {
    return strings;
  }

  public List<ListFactory.Some> somes() {
    return somes;
  }
}
