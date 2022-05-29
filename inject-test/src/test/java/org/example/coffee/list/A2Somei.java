package org.example.coffee.list;

import javax.annotation.Priority;
import jakarta.inject.Singleton;

@Singleton
@Priority(22)
public class A2Somei implements Somei {

  @Override
  public String some() {
    return "a2";
  }
}
