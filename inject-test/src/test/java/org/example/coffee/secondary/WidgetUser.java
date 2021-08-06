package org.example.coffee.secondary;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
public class WidgetUser {

  private final Widget widget;

  public WidgetUser(@Named("doesNotExist") Widget widget) {
    this.widget = widget;
  }

  public String wid() {
    return widget.wid();
  }
}
