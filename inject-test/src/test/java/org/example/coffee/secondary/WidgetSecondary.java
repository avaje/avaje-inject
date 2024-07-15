package org.example.coffee.secondary;

import io.avaje.inject.Secondary;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
@Secondary
@Named("doesNotExist")
public class WidgetSecondary implements Widget {

  @Override
  public String wid() {
    return "second";
  }
}
