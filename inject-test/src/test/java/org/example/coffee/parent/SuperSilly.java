package org.example.coffee.parent;

import jakarta.inject.Singleton;

@Singleton
public class SuperSilly extends Silly {

  @Override
  String con() {
    return "SuperSilly";
  }
}
