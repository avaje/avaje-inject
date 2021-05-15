package org.example.requestscope;

import io.avaje.inject.ApplicationScope;
import io.avaje.inject.RequestScope;
import org.example.request.AService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequestScopedBeanTest {

  @Test
  void preDestroy() {

    MyRequestPreDestroy bean;
    try (RequestScope requestScope = ApplicationScope.newRequestScope()
      .build()) {
      bean = requestScope.get(MyRequestPreDestroy.class);
      assertThat(bean.isFiredPreDestroy()).isFalse();
    }
    assertThat(bean.isFiredPreDestroy()).isTrue();
  }

  @Test
  void newRequestScope() {

    MyReqThing myReqThing = new MyReqThing();

    MyRequestOne myRequestOne;
    try (RequestScope requestScope = ApplicationScope.newRequestScope()
      .withBean(ReqThing.class, myReqThing)
      .build()) {

      myRequestOne = requestScope.get(MyRequestOne.class);
      assertThat(myRequestOne.getReqThing().hello()).isEqualTo("MyReqThing");

      assertThat(myRequestOne.isFiredPostConstruct()).isTrue();
      assertThat(myRequestOne.isFiredMethodInjection()).isTrue();

      assertThat(myRequestOne).isNotNull();
      assertThat(myRequestOne.getReqThing()).isSameAs(myReqThing);

      AService service = requestScope.get(AService.class);
      assertThat(myRequestOne.getService()).isSameAs(service);
      assertThat(myRequestOne.getOtherService()).isSameAs(service);
      assertThat(myRequestOne.getOtherReqThing()).isSameAs(myReqThing);

      // assert we get back the same instance
      MyRequestOne myRequestOneAgain = requestScope.get(MyRequestOne.class);
      assertThat(myRequestOneAgain).isSameAs(myRequestOne);
    }

    // assert request scoped closable was closed
    assertThat(myRequestOne.isFiredClose()).isTrue();
  }

  @Test
  void provideNamedForRequestBeans() {

    // providing named implementations of RPump
    RPump redPump = new RPumpF("red");
    RPump bluePump = new RPumpF("blue");

    try (RequestScope requestScope = ApplicationScope.newRequestScope()
      .withBean("red", RPump.class, redPump)
      .withBean("blue", RPump.class, bluePump)
      .build()) {

      final RedsRStuff redsRStuff = requestScope.get(RedsRStuff.class);
      assertThat(redsRStuff.pump()).isEqualTo("red");

      final RStuff redsRStuff2 = requestScope.get(RStuff.class, "reds");
      assertThat(redsRStuff2.stuff()).isEqualTo("stuff_red");
      assertThat(redsRStuff2).isSameAs(redsRStuff);


      final BluesRStuff bluesRStuff = requestScope.get(BluesRStuff.class);
      assertThat(bluesRStuff.pump()).isEqualTo("blue");

      final RStuff blues2 = requestScope.get(RStuff.class, "blues");
      assertThat(blues2.stuff()).isEqualTo("stuff_blue");
      assertThat(blues2).isSameAs(bluesRStuff);

      final ComboRStuff comboRStuff = requestScope.get(ComboRStuff.class);
      assertThat(comboRStuff.stuff()).isEqualTo("stuff_red stuff_blue");
    }
  }

  static class MyReqThing implements ReqThing {
    @Override
    public String hello() {
      return "MyReqThing";
    }
  }
}
