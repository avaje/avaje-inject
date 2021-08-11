package org.example.coffee.primary;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class UseNamedPEmailer {

  private final PEmailer emailer;
  private final PEmailer other;
  private final PEmailer otherPEmailer;

  public UseNamedPEmailer(@Named("Other") PEmailer emailer, PEmailer other, PEmailer otherPEmailer) {
    this.emailer = emailer;
    this.other = other;
    this.otherPEmailer = otherPEmailer;
  }

  public String emailNamed() {
    return emailer.email();
  }

  public String emailOther() {
    return other.email();
  }

  public String emailOtherPEmailer() {
    return otherPEmailer.email();
  }

}
