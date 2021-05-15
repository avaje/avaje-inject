package org.example.coffee.parent;

import io.avaje.inject.ApplicationScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SuperSilly extends Silly which is a concrete <code>@Singleton</code>.
 */
class ConcreteParentTest {

  @Test
  void simple_getBean_base() {
    Silly bean = ApplicationScope.get(Silly.class);
    assertThat(bean.con()).isEqualTo("Silly");
  }

  @Test
  void simple_getBean() {
    SuperSilly bean = ApplicationScope.get(SuperSilly.class);
    assertThat(bean.con()).isEqualTo("SuperSilly");
  }

  @Test
  void simple_getBean_viaName() {
    Silly beanViaName = ApplicationScope.get(Silly.class, "Super");
    assertThat(beanViaName.con()).isEqualTo("SuperSilly");

    SuperSilly specificBean = ApplicationScope.get(SuperSilly.class);
    assertThat(specificBean).isSameAs(beanViaName);
  }

}
