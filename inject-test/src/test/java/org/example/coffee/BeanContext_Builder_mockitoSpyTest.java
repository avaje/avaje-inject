package org.example.coffee;

import io.avaje.inject.BeanContext;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BeanContext_Builder_mockitoSpyTest {

  @Test
  public void withBeans_asMocks() {

    Pump pump = mock(Pump.class);
    Grinder grinder = mock(Grinder.class);

    try (BeanContext context = BeanContext.newBuilder()
      .withBeans(pump, grinder)
      .build()) {

      CoffeeMaker coffeeMaker = context.getBean(CoffeeMaker.class);
      coffeeMaker.makeIt();

      Pump pump1 = context.getBean(Pump.class);
      Grinder grinder1 = context.getBean(Grinder.class);

      assertThat(pump1).isSameAs(pump);
      assertThat(grinder1).isSameAs(grinder);

      verify(pump).pumpWater();
      verify(grinder).grindBeans();
    }
  }

  @Test
  public void withMockitoSpy_noSetup_expect_spyUsed() {

    try (BeanContext context = BeanContext.newBuilder()
      .withSpy(Pump.class)
      .build()) {

      CoffeeMaker coffeeMaker = context.getBean(CoffeeMaker.class);
      assertThat(coffeeMaker).isNotNull();
      coffeeMaker.makeIt();

      Pump pump = context.getBean(Pump.class);
      verify(pump).pumpWater();
    }
  }

  @Test
  public void withMockitoSpy_postLoadSetup_expect_spyUsed() {

    try (BeanContext context = BeanContext.newBuilder()
      .withSpy(Pump.class)
      .withSpy(Grinder.class)
      .build()) {

      // setup after load()
      Pump pump = context.getBean(Pump.class);
      doNothing().when(pump).pumpWater();

      CoffeeMaker coffeeMaker = context.getBean(CoffeeMaker.class);
      assertThat(coffeeMaker).isNotNull();
      coffeeMaker.makeIt();

      verify(pump).pumpWater();

      Grinder grinder = context.getBean(Grinder.class);
      verify(grinder).grindBeans();
    }
  }

  @Test
  public void withMockitoSpy_expect_spyUsed() {

    try (BeanContext context = BeanContext.newBuilder()
      .withSpy(Pump.class, pump -> {
        // setup the spy
        doNothing().when(pump).pumpWater();
      })
      .build()) {

      // or setup here ...
      Pump pump = context.getBean(Pump.class);
      doNothing().when(pump).pumpSteam();

      // act
      CoffeeMaker coffeeMaker = context.getBean(CoffeeMaker.class);
      coffeeMaker.makeIt();

      verify(pump).pumpWater();
      verify(pump).pumpSteam();
    }
  }

  @Test
  public void withMockitoSpy_whenPrimary_expect_spyUsed() {

    try (BeanContext context = BeanContext.newBuilder()
      .withSpy(PEmailer.class) // has a primary
      .build()) {

      UserOfPEmailer user = context.getBean(UserOfPEmailer.class);
      PEmailer emailer = context.getBean(PEmailer.class);

      user.email();
      verify(emailer).email();
    }
  }

  @Test
  public void withMockitoSpy_whenOnlySecondary_expect_spyUsed() {

    try (BeanContext context = BeanContext.newBuilder()
      .withSpy(Widget.class) // only secondary
      .build()) {

      WidgetUser widgetUser = context.getBean(WidgetUser.class);

      String val = widgetUser.wid();
      assertThat(val).isEqualTo("second");
      Widget widget = context.getBean(Widget.class);
      verify(widget).wid();

      // these are the same (secondary only)
      WidgetSecondary widgetSecondary = context.getBean(WidgetSecondary.class);
      //assertThat(widget).isSameAs(widgetSecondary);
      assertThat(widgetSecondary).isNull();
    }
  }

  @Test
  public void withMockitoSpy_whenSecondary_expect_spyUsed() {

    try (BeanContext context = BeanContext.newBuilder()
      .withSpy(Something.class) // has a secondary and a normal
      .build()) {

      Unused unused = context.getBean(Unused.class);
      Something something = context.getBean(Something.class);
      String result = unused.doSomething();
      verify(something).doStuff();

      // someImpl has higher precedence than the Secondary
      assertThat(result).isEqualTo("SomeImpl");

      SomeImpl someImpl = context.getBean(SomeImpl.class);
      SomeImplBean someImplBean = context.getBean(SomeImplBean.class);
      assertThat(someImpl).isNull();
      assertThat(someImplBean).isNull();
      //assertThat(something).isSameAs(someImpl);
      //assertThat(someImpl).isNotSameAs(someImplBean);
    }
  }

  @Test
  public void withMockitoMock_expect_mockUsed() {

    AtomicReference<Grinder> mock = new AtomicReference<>();

    try (BeanContext context = BeanContext.newBuilder()
      .withMock(Pump.class)
      .withMock(Grinder.class, grinder -> {
        // setup the mock
        when(grinder.grindBeans()).thenReturn("stub response");
        mock.set(grinder);
      })
      .build()) {

      Grinder grinder = context.getBean(Grinder.class);
      assertThat(grinder).isSameAs(mock.get());

      CoffeeMaker coffeeMaker = context.getBean(CoffeeMaker.class);
      assertThat(coffeeMaker).isNotNull();
      coffeeMaker.makeIt();

      verify(grinder).grindBeans();
    }
  }

}
