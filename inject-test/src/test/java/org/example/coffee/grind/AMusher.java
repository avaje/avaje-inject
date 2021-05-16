package org.example.coffee.grind;

import io.avaje.inject.PostConstruct;
import io.avaje.inject.PreDestroy;
import javax.inject.Singleton;

@Singleton
public class AMusher {

  private int countInit;
  private int countClose;

  String mush() {
    return "amush";
  }

  @PostConstruct
  public void postConstruct() {
    countInit++;
  }

  @PreDestroy
  public void preDestroy() {
    countClose++;
  }

  public int getCountInit() {
    return countInit;
  }

  int getCountClose() {
    return countClose;
  }
}
