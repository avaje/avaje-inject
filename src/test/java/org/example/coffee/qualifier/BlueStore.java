package org.example.coffee.qualifier;

import javax.inject.Named;
import javax.inject.Singleton;

@Named("Blue")
//@Blue
@Singleton
public class BlueStore implements SomeStore {

  @Override
  public String store() {
    return "blue";
  }
}
