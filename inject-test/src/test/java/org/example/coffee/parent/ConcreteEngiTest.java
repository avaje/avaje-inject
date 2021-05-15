package org.example.coffee.parent;

import io.avaje.inject.ApplicationScope;
import org.example.coffee.parent.sub.Engi;
import org.example.coffee.parent.sub.PetEngi;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConcreteEngiTest {

  @Test
  void getBean_usingSuperClassTypeAndName() {
    LightPetEngi lightByType = ApplicationScope.get(LightPetEngi.class);
    PetEngi lightByName = ApplicationScope.get(PetEngi.class, "Lite");
    Engi engiByName = ApplicationScope.get(Engi.class, "Lite");

    assertThat(lightByType.ignite()).isEqualTo("lightPetEngi");
    assertThat(lightByName).isSameAs(lightByType);
    assertThat(engiByName).isSameAs(lightByType);
  }

  @Test
  void getBean_usingSuperClassTypeAndName_other() {
    Engi engi = ApplicationScope.get(Engi.class, "Des");
    assertThat(engi.ignite()).isEqualTo("desEngi");
    //DesEngi desEngi = ApplicationScope.get(DesEngi.class);
    //assertThat(desEngi).isSameAs(engi);
  }

  @Test
  void getBean_usingSuperClassTypeAndName_NoImpliedName() {
    NoImpliedNameEngi engi = ApplicationScope.get(NoImpliedNameEngi.class);
    assertThat(engi.ignite()).isEqualTo("NoImpliedNameEngi");
  }
}
