package org.example.coffee.parent;

import javax.inject.Singleton;

@Singleton
public class SuperSilly extends Silly {

  @Override
  String con() {
    return "SuperSilly";
  }
}
