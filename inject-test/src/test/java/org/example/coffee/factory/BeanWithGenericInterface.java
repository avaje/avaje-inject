package org.example.coffee.factory;

import java.io.File;
import java.util.function.Consumer;

public class BeanWithGenericInterface implements Consumer<File> {
  @Override
  public void accept(File o) {
    // do nothing
  }
}
