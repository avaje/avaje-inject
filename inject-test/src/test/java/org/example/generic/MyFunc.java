package org.example.generic;

import java.util.function.Function;

public class MyFunc implements Function<Object, Object> {

  @Override
  public Object apply(Object o) {
    return "echo: " + o;
  }
}
