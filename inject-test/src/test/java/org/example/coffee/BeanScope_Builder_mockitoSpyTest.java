package org.example.coffee;

import io.avaje.inject.BeanScope;
import org.example.coffee.factory.SomeImpl;
import org.example.coffee.factory.SomeImplBean;
import org.example.coffee.factory.Unused;
import org.example.coffee.factory.other.Something;
import org.example.coffee.grind.Grinder;
import org.example.coffee.primary.PEmailer;
import org.example.coffee.primary.UserOfPEmailer;
import org.example.coffee.secondary.Widget;
import org.example.coffee.secondary.WidgetSecondary;
import org.example.coffee.secondary.WidgetUser;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class BeanScope_Builder_mockitoSpyTest {

  @Test
  public void withBeans_asMocks() {

    Pump pump = mock(Pump.class);
    Grinder grinder = mock(Grinder.class);

    try (BeanScope context = BeanScope.newBuilder()
      .withBeans(pump, grinder)
      .build()) {

      CoffeeMaker coffeeMaker = context.get(CoffeeMaker.class);
      coffeeMaker.makeIt();

      Pump pump1 = context.get(Pump.class);
      Grinder grinder1 = context.get(Grinder.class);

      assertThat(pump1).isSameAs(pump);
      assertThat(grinder1).isSameAs(grinder);

      verify(pump).pumpWater();
      verify(grinder).grindBeans();
    }
  }

  @Test
  public void withMockitoSpy_noSetup_expect_spyUsed() {

    try (BeanScope context = BeanScope.newBuilder()
      .forTesting()
      .withSpy(Pump.class)
      .build()) {

      CoffeeMaker coffeeMaker = context.get(CoffeeMaker.class);
      assertThat(coffeeMaker).isNotNull();
      coffeeMaker.makeIt();

      Pump pump = context.get(Pump.class);
      verify(pump).pumpWater();
    }
  }

  @Test
  public void withMockitoSpy_postLoadSetup_expect_spyUsed() {

    try (BeanScope context = BeanScope.newBuilder()
      .forTesting()
      .withSpy(Pump.class)
      .withSpy(Grinder.class)
      .build()) {

      // setup after load()
      Pump pump = context.get(Pump.class);
      doNothing().when(pump).pumpWater();

      CoffeeMaker coffeeMaker = context.get(CoffeeMaker.class);
      assertThat(coffeeMaker).isNotNull();
      coffeeMaker.makeIt();

      verify(pump).pumpWater();

      Grinder grinder = context.get(Grinder.class);
      verify(grinder).grindBeans();
    }
  }

  @Test
  public void withMockitoSpy_expect_spyUsed() {

    try (BeanScope context = BeanScope.newBuilder()
      .forTesting()
      .withSpy(Pump.class, pump -> {
        // setup the spy
        doNothing().when(pump).pumpWater();
      })
      .build()) {

      // or setup here ...
      Pump pump = context.get(Pump.class);
      doNothing().when(pump).pumpSteam();

      // act
      CoffeeMaker coffeeMaker = context.get(CoffeeMaker.class);
      coffeeMaker.makeIt();

      verify(pump).pumpWater();
      verify(pump).pumpSteam();
    }
  }

  @Test
  public void withMockitoSpy_whenPrimary_expect_spyUsed() {

    try (BeanScope context = BeanScope.newBuilder()
      .forTesting()
      .withSpy(PEmailer.class) // has a primary
      .build()) {

      UserOfPEmailer user = context.get(UserOfPEmailer.class);
      PEmailer emailer = context.get(PEmailer.class);

      user.email();
      verify(emailer).email();
    }
  }

  @Test
  public void withMockitoSpy_whenOnlySecondary_expect_spyUsed() {

    try (BeanScope context = BeanScope.newBuilder()
      .forTesting()
      .withSpy(Widget.class) // only secondary
      .build()) {

      WidgetUser widgetUser = context.get(WidgetUser.class);

      String val = widgetUser.wid();
      assertThat(val).isEqualTo("second");
      Widget widget = context.get(Widget.class);
      verify(widget).wid();

      // these are the same (secondary only)
      WidgetSecondary widgetSecondary = context.get(WidgetSecondary.class);
      assertThat(widget).isSameAs(widgetSecondary);
    }
  }

  /**
   * Still matches when only 1 candidate even if the qualifier name doesn't exist.
   */
  @Test
  public void withNamed_when_qualifierNameDoesNotExist_but_onlyOneCandidate() {

    try (BeanScope context = BeanScope.newBuilder()
      .build()) {

      WidgetUser widgetUser = context.get(WidgetUser.class);

      String val = widgetUser.wid();
      assertThat(val).isEqualTo("second");
      Widget widget = context.get(Widget.class);

      // these are the same (secondary only)
      WidgetSecondary widgetSecondary = context.get(WidgetSecondary.class);
      assertThat(widget).isSameAs(widgetSecondary);
    }
  }

  @Test
  public void withMockitoSpy_whenSecondary_expect_spyUsed() {

    try (BeanScope context = BeanScope.newBuilder()
      .forTesting()
      .withSpy(Something.class) // has a secondary and a normal
      .build()) {

      Unused unused = context.get(Unused.class);
      Something something = context.get(Something.class);
      String result = unused.doSomething();
      verify(something).doStuff();

      // someImpl has higher precedence than the Secondary
      assertThat(result).isEqualTo("SomeImpl");

      SomeImpl someImpl = context.get(SomeImpl.class);
      assertThat(someImpl).isNull();
      SomeImplBean someImplBean = context.get(SomeImplBean.class);
      assertThat(something).isNotSameAs(someImplBean);
    }
  }

  @Test
  public void withMockitoMock_expect_mockUsed() {

    AtomicReference<Grinder> mock = new AtomicReference<>();

    try (BeanScope context = BeanScope.newBuilder()
      .forTesting()
      .withMock(Pump.class)
      .withMock(Grinder.class, grinder -> {
        // setup the mock
        when(grinder.grindBeans()).thenReturn("stub response");
        mock.set(grinder);
      })
      .build()) {

      Grinder grinder = context.get(Grinder.class);
      assertThat(grinder).isSameAs(mock.get());

      CoffeeMaker coffeeMaker = context.get(CoffeeMaker.class);
      assertThat(coffeeMaker).isNotNull();
      coffeeMaker.makeIt();

      verify(grinder).grindBeans();
    }
  }

}
