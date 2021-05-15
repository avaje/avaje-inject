package org.example.circular;

import io.avaje.inject.ApplicationScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CupholderTest {

  @Test
  void circularDependency_via_providerInterface() {

    Cupholder cupholder = ApplicationScope.get(Cupholder.class);
    String hello = cupholder.hello();

    assertThat(hello).isEqualTo("CupHelloSeatHello");

    // check circular binding
    Seat seat = ApplicationScope.get(Seat.class);
    Cupholder seatCupholder = seat.getCupholder();

    assertThat(seatCupholder).isSameAs(cupholder);
  }
}
