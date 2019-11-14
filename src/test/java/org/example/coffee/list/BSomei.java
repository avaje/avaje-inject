package org.example.coffee.list;

import javax.annotation.Priority;
import javax.inject.Singleton;

@Singleton
@Priority(1)
public class BSomei implements Somei {

  @Override
  public String some() {
    return "b";
  }
}
