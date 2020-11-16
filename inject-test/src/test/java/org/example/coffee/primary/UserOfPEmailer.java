package org.example.coffee.primary;

import jakarta.inject.Singleton;

@Singleton
public class UserOfPEmailer {

  private final PEmailer emailer;

  public UserOfPEmailer(PEmailer emailer) {
    this.emailer = emailer;
  }

  public String email() {
    return emailer.email();
  }
}
