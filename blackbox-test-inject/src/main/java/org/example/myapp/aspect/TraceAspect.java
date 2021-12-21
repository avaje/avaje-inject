package org.example.myapp.aspect;

import java.util.ArrayList;
import java.util.List;

public class TraceAspect {

  static List<String> trace = new ArrayList<>();

  static void add(String value) {
    trace.add(value);
  }

  public static void clear() {
    trace.clear();
  }

  public static List<String> obtain() {
    List<String> copy = new ArrayList<>(trace);
    trace.clear();
    return copy;
  }

}
