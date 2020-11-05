package org.example.coffee.secondary;

import jakarta.inject.Singleton;

@Singleton
public class WidgetUser {

  private final Widget widget;

  public WidgetUser(Widget widget) {
    this.widget = widget;
  }

  public String wid() {
    return widget.wid();
  }
}
