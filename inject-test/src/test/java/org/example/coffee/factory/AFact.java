package org.example.coffee.factory;

import io.avaje.inject.PostConstruct;
import io.avaje.inject.PreDestroy;

public class AFact {

  private final String userHome;

  private int countConstruct;
  private int countDestroy;

  public AFact(String userHome) {
    this.userHome = userHome;
  }

  public String a() {
    return userHome;
  }

  @PostConstruct
  public void postConstruct() {
    countConstruct++;
  }

  @PreDestroy
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
