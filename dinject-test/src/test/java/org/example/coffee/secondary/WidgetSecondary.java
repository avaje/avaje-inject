package org.example.coffee.secondary;

import io.dinject.Secondary;

import javax.inject.Singleton;

@Singleton
@Secondary
public class WidgetSecondary implements Widget {

  @Override
  public String wid() {
    return "second";
  }
}
