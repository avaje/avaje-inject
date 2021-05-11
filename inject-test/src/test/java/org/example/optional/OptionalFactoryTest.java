package org.example.optional;

import io.avaje.inject.SystemContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OptionalFactoryTest {

  @Test
  void injectOptional_viaConstructor() {
    AllQue allQue = SystemContext.getBean(AllQue.class);
    assertThat(allQue.whichSet()).isEqualTo("f:Optional[frodo]s:Optional[sam]b:Optional.empty");

    String msg = allQue.frodoPush("hello");
    assertThat(msg).isEqualTo("hello|frodo");
  }

  @Test
  void injectOptional_viaField() {
    AllQue2 allQue = SystemContext.getBean(AllQue2.class);
    assertThat(allQue.whichSet()).isEqualTo("f:frodos:Optional[sam]b:Optional.empty");
  }

  @Test
  void injectOptional_viaMethod() {
    AllQue3 allQueViaMethodInjection = SystemContext.getBean(AllQue3.class);
    assertThat(allQueViaMethodInjection.whichSet()).isEqualTo("f:frodos:samb:null");
  }

  @Test
  void injectNullable_viaMethod() {
    AllQue4 allQue = SystemContext.getBean(AllQue4.class);
    assertThat(allQue.whichSet()).isEqualTo("f:frodos:samb:null");
  }

  @Test
  void injectNullable_viaConstructor() {
    AllQue5 allQue = SystemContext.getBean(AllQue5.class);
    assertThat(allQue.whichSet()).isEqualTo("f:frodos:samb:null");
  }

  @Test
  void injectNullable_viaField() {
    AllQue6 allQue = SystemContext.getBean(AllQue6.class);
    assertThat(allQue.whichSet()).isEqualTo("f:frodos:samb:null");
  }

}
