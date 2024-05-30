package org.example.myapp;

import java.util.List;

import jakarta.inject.Singleton;

@Singleton
public class ListService {

  private final List<String> strings;
  private final List<ListFactory.Some> somes;

  public ListService(List<String> strings, List<ListFactory.Some> somes) {
    this.strings = strings;
    this.somes = somes;
  }

  public List<String> strings() {
    return strings;
  }

  public List<ListFactory.Some> somes() {
    return somes;
  }
}
