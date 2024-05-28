package org.example.myapp;

import java.util.List;

import jakarta.inject.Singleton;

@Singleton
public class ListService {

  private final List<String> strings;

  public ListService(List<String> strings) {
    this.strings = strings;
  }

  public List<String> strings() {
    return strings;
  }
}
