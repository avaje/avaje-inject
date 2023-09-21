package org.example.myapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyDestroyOrder {

  private static final List<String> ordering = Collections.synchronizedList(new ArrayList<>());

  public static void add(String val){
    ordering.add(val);
  }

  public static List<String> ordering() {
    return ordering;
  }
}
