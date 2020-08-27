package org.example.coffee.factory;

import io.avaje.inject.core.BeanLifecycle;

public class AFact implements BeanLifecycle {


  private final String userHome;

  private int countConstruct;
  private int countDestroy;

  public AFact(String userHome) {
    this.userHome = userHome;
  }

  public String a() {
    return userHome;
  }

  @Override
  public void postConstruct() {
    countConstruct++;
  }

  @Override
  public void preDestroy() {
    countDestroy++;
  }

  int getCountConstruct() {
    return countConstruct;
  }

  int getCountDestroy() {
    return countDestroy;
  }
}
