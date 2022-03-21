package org.example.optional;

import io.avaje.inject.xtra.ApplicationScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OptionalFactoryTest {

  @Test
  void injectOptional_viaConstructor() {
    AllQue allQue = ApplicationScope.get(AllQue.class);
    assertThat(allQue.whichSet()).isEqualTo("f:Optional[frodo]s:Optional[sam]b:Optional.empty");

    String msg = allQue.frodoPush("hello");
    assertThat(msg).isEqualTo("hello|frodo");
  }

  @Test
  void injectOptional_viaField() {
    AllQue2 allQue = ApplicationScope.get(AllQue2.class);
    assertThat(allQue.whichSet()).isEqualTo("f:frodos:Optional[sam]b:Optional.empty");
  }

  @Test
  void injectOptional_viaMethod() {
    AllQue3 allQueViaMethodInjection = ApplicationScope.get(AllQue3.class);
    assertThat(allQueViaMethodInjection.whichSet()).isEqualTo("f:frodos:samb:null");
  }

  @Test
  void injectNullable_viaMethod() {
    AllQue4 allQue = ApplicationScope.get(AllQue4.class);
    assertThat(allQue.whichSet()).isEqualTo("f:frodos:samb:null");
  }

  @Test
  void injectNullable_viaConstructor() {
    AllQue5 allQue = ApplicationScope.get(AllQue5.class);
    assertThat(allQue.whichSet()).isEqualTo("f:frodos:samb:null");
  }

  @Test
  void injectNullable_viaField() {
    AllQue6 allQue = ApplicationScope.get(AllQue6.class);
    assertThat(allQue.whichSet()).isEqualTo("f:frodos:samb:null");
  }

}
