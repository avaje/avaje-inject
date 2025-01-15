package org.example.myapp.generic;

import java.util.function.BiConsumer;

public class MyConsumer implements BiConsumer<MyA, MyB> {

  @Override
  public void accept(MyA t, MyB u) {}
}
