package org.example.coffee;

import io.dinject.BeanContext;
import io.dinject.BeanContextBuilder;
import org.example.coffee.provider.ProvOther;
import org.example.coffee.provider.ProvOther2;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ProviderTest {

  @Test
  public void test() {

    try (BeanContext context = new BeanContextBuilder().build()) {

      ProvOther bean = context.getBean(ProvOther.class);
      String other = bean.other();
      assertThat(other).isEqualTo("mush mush beans");

      ProvOther2 bean2 = context.getBean(ProvOther2.class);
      assertThat(bean2.getaProv()).isSameAs(bean.getaProv());
    }

  }
}
