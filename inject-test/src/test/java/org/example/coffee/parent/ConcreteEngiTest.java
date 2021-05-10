package org.example.coffee.parent;

import io.avaje.inject.SystemContext;
import org.example.coffee.parent.sub.Engi;
import org.example.coffee.parent.sub.PetEngi;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConcreteEngiTest {

  @Test
  void getBean_usingSuperClassTypeAndName() {
    LightPetEngi lightByType = SystemContext.getBean(LightPetEngi.class);
    PetEngi lightByName = SystemContext.getBean(PetEngi.class, "Lite");
    Engi engiByName = SystemContext.getBean(Engi.class, "Lite");

    assertThat(lightByType.ignite()).isEqualTo("lightPetEngi");
    assertThat(lightByName).isSameAs(lightByType);
    assertThat(engiByName).isSameAs(lightByType);
  }

  @Test
  void getBean_usingSuperClassTypeAndName_other() {
    Engi engi = SystemContext.getBean(Engi.class, "Des");
    assertThat(engi.ignite()).isEqualTo("desEngi");
    //DesEngi desEngi = SystemContext.getBean(DesEngi.class);
    //assertThat(desEngi).isSameAs(engi);
  }

  @Test
  void getBean_usingSuperClassTypeAndName_NoImpliedName() {
    NoImpliedNameEngi engi = SystemContext.getBean(NoImpliedNameEngi.class);
    assertThat(engi.ignite()).isEqualTo("NoImpliedNameEngi");
  }
}
