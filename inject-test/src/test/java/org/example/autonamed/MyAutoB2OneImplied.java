package org.example.autonamed;

import jakarta.inject.Singleton;

import javax.annotation.PostConstruct;

@Singleton
public class MyAutoB2OneImplied {

  final AutoB2 one;

  public MyAutoB2OneImplied(AutoB2 one) throws Exception {
    this.one = one;
  }

  public String one() {
    return one.who();
  }

  @PostConstruct
  public void postInit() {

  }

}
