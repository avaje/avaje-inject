package org.example.custom;

import org.example.MyCustomScope;
import org.example.coffee.CoffeeMaker;
import org.example.custom.loc.LocalExternal;

@MyCustomScope
public class FooCustom {

  /**
   * CoffeeMaker is provided by the "default" scope and that is "good enough" for now (could be better/tighter here).
   */
  final CoffeeMaker coffeeMaker;
  final LocalExternal external;

  public FooCustom(CoffeeMaker coffeeMaker, LocalExternal external) {
    this.coffeeMaker = coffeeMaker;
    this.external = external;
  }
}
