package org.example.myapp.supplier;

import io.avaje.inject.Component;

import java.util.function.Supplier;

@Component
class MySupConsumer {

  private final Supplier<MySupFactory.SupFoo> supplier;
  private MySupFactory.SupFoo supFoo;

  MySupConsumer(Supplier<MySupFactory.SupFoo> supplier) {
    this.supplier = supplier;
    supFoo = supplier.get();
  }

  MySupFactory.SupFoo current() {
    return supFoo;
  }

  MySupFactory.SupFoo next() {
    supFoo = supplier.get();
    return supFoo;
  }
}
