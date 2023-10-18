package org.example.myapp.conditional;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresProperty;
import org.example.myapp.aspect.MyAround;

@Component
@RequiresProperty(value = "factory")
public class WithAspectConditional {

  @MyAround
  void test(String str) {
    // does nothing
  }
}
