package org.example.custom;

import org.example.MyCustomScope;

@MyCustomScope
public class FooCustom {

  final LocalExternal external;

  public FooCustom(LocalExternal external) {
    this.external = external;
  }
}
