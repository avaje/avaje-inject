package org.example.circular;

import io.avaje.inject.SystemContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CupholderTest {

  @Test
  void circularDependency_via_providerInterface() {

    Cupholder cupholder = SystemContext.getBean(Cupholder.class);
    String hello = cupholder.hello();

    assertThat(hello).isEqualTo("CupHelloSeatHello");

    // check circular binding
    Seat seat = SystemContext.getBean(Seat.class);
    Cupholder seatCupholder = seat.getCupholder();

    assertThat(seatCupholder).isSameAs(cupholder);
  }
}
