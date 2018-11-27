package org.example.coffee.primary;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class UserOfPEmailer {

  private final PEmailer emailer;

  public UserOfPEmailer(@Named("prime") PEmailer emailer) {
    this.emailer = emailer;
  }

  public String email() {
    return emailer.email();
  }
}
