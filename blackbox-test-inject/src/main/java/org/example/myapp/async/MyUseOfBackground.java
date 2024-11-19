package org.example.myapp.async;

import io.avaje.inject.AsyncBean;
import io.avaje.inject.Component;
import jakarta.inject.Named;

@AsyncBean
@Component
public class MyUseOfBackground {

  private final BackgroundBean backgroundBean;

  public MyUseOfBackground(@Named("single") BackgroundBean backgroundBean) {
    this.backgroundBean = backgroundBean;
  }
}
