package org.example.myapp;

import io.avaje.inject.PreDestroy;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyDestroyOrder2 {

  private static final List<String> ordering = Collections.synchronizedList(new ArrayList<>());

  public static void add(String val) {
    ordering.add(val);
  }

  public static List<String> ordering() {
    return ordering;
  }

  @Singleton
  public static class One {
    @PreDestroy
    public void close() {
      add("One");
    }
  }

  @Singleton
  public static class Two {

    final One one;

    public Two(One one) {
      this.one = one;
    }

    @PreDestroy
    public void close() {
      add("Two");
    }
  }

}
