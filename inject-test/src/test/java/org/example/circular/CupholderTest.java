package org.example.circular;

import io.avaje.inject.BeanScope;
import io.avaje.inject.xtra.ApplicationScope;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CupholderTest {

  @Test
  void mockOne() {
    try (BeanScope beanScope = BeanScope.builder().forTesting()
      .mock(Cupholder.class)
      .build()) {

      Cupholder cupholder = beanScope.get(Cupholder.class);
      assertThat(cupholder.hello()).isNull();

      List<Cupholder> all = beanScope.list(Cupholder.class);
      assertThat(all).hasSize(1);
    }
  }

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
