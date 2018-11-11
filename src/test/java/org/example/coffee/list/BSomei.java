package org.example.coffee.list;

import javax.inject.Singleton;

@Singleton
public class BSomei implements Somei {

  @Override
  public String some() {
    return "b";
  }
}
