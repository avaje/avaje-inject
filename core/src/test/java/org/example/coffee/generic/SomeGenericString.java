package org.example.coffee.generic;

import javax.inject.Singleton;

@Singleton
public class SomeGenericString implements SomeGeneric<String> {

  @Override
  public String process(String data) {
    return data + " stuff";
  }
}
