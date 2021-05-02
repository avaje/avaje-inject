package org.example.circular;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class Cupholder {

  public final Provider<Seat> seatProvider;

  /**
   * Resolve circular dependency via Provider interface rather than field injection.
   */
  @Inject
  public Cupholder(Provider<Seat> seatProvider) {
    this.seatProvider = seatProvider;
  }

  public String hello() {
    return "CupHello" + seatProvider.get().hello();
  }
}
