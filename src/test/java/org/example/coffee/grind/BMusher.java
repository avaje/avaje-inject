package org.example.coffee.grind;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;

@Singleton
public class BMusher {

  private int countInit;
  private int countClose;

  String mush() {
    return "Bmush";
  }

  @PostConstruct
  void init() {
    countInit++;
  }

  @PreDestroy
  void close() {
    countClose++;
  }

  int getCountInit() {
    return countInit;
  }

  int getCountClose() {
    return countClose;
  }
}
