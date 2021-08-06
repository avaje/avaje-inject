package org.example.coffee.secondary;

import javax.inject.Named;
import javax.inject.Singleton;

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
