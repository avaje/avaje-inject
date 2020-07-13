package io.dinject;

import org.example.coffee.list.ASomei;
import org.example.coffee.list.BSomei;
import org.example.coffee.list.Somei;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SystemContextTest {

  @Test
  public void getBeans_withPriority() {

    final List<Somei> beans = SystemContext.getBeans(Somei.class);
    assertThat(beans).hasSize(2);

    assertThat(beans.get(0)).isInstanceOf(BSomei.class);
    assertThat(beans.get(1)).isInstanceOf(ASomei.class);
  }

  @Test
  public void getBeansUnsorted_withPriority() {

    final List<Somei> beans = SystemContext.getBeansUnsorted(Somei.class);
    assertThat(beans).hasSize(2);
    // can't assert bean order
  }
}
