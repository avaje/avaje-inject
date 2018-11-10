package org.example.coffee.factory;

public class AFact {

  private final String userHome;

  public AFact(String userHome) {
    this.userHome = userHome;
  }

  public String a() {
    return userHome;
  }
}
