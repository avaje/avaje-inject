package org.example.coffee.list;

import javax.inject.Singleton;

@Singleton
public class ASomei implements Somei {

  @Override
  public String some() {
    return "a";
  }
}
