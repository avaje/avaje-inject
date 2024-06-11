package org.example.injectextension;

import io.avaje.inject.test.InjectJunitExtension;
import jakarta.inject.Inject;
import org.example.coffee.CoffeeMaker;
import org.example.coffee.Pump;
import org.example.coffee.grind.Grinder;
import org.example.missing.MFoo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(InjectJunitExtension.class)
public class WithExtnPrivateFieldsTest {

  @Mock
  private Pump pump;

  @Mock
  private Grinder grinder;

  @Spy
  private CoffeeMaker coffeeMaker;

  @Inject
  private MFoo foo;

  @Test
  void when_privateField_expect_fieldsSet() {
    assertThat(pump).isNotNull();
    assertThat(grinder).isNotNull();
    assertThat(coffeeMaker).isNotNull();
    assertThat(foo).isNotNull();
  }

}
