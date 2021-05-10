package org.example.coffee.parent;

import io.avaje.inject.SystemContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SuperSilly extends Silly which is a concrete <code>@Singleton</code>.
 */
class ConcreteParentTest {

  @Test
  void simple_getBean_base() {
    Silly bean = SystemContext.getBean(Silly.class);
    assertThat(bean.con()).isEqualTo("Silly");
  }

  @Test
  void simple_getBean() {
    SuperSilly bean = SystemContext.getBean(SuperSilly.class);
    assertThat(bean.con()).isEqualTo("SuperSilly");
  }

  @Test
  void simple_getBean_viaName() {
    Silly beanViaName = SystemContext.getBean(Silly.class, "Super");
    assertThat(beanViaName.con()).isEqualTo("SuperSilly");

    SuperSilly specificBean = SystemContext.getBean(SuperSilly.class);
    assertThat(specificBean).isSameAs(beanViaName);
  }

}
