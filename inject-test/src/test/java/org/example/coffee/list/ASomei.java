package org.example.coffee.list;

import javax.annotation.Priority;
import jakarta.inject.Singleton;

@Singleton
@Priority(2)
public class ASomei implements Somei {

  @Override
  public String some() {
    return "a";
  }
}
