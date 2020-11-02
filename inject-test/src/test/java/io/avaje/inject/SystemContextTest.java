package io.avaje.inject;

import org.example.coffee.fruit.Fruit;
import org.example.coffee.list.A2Somei;
import org.example.coffee.list.ASomei;
import org.example.coffee.list.BSomei;
import org.example.coffee.list.Somei;
import org.junit.jupiter.api.Test;

import javax.annotation.Priority;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SystemContextTest {

  @Test
  public void getBeansByPriority() {

    final List<Somei> beans = SystemContext.context().getBeansByPriority(Somei.class, Priority.class);
    assertThat(beans).hasSize(3);

    assertThat(beans.get(0)).isInstanceOf(BSomei.class);
    assertThat(beans.get(1)).isInstanceOf(ASomei.class);
    assertThat(beans.get(2)).isInstanceOf(A2Somei.class);
  }

  @Test
  public void getBeansByPriority_withAnnotation() {

    final List<Somei> beans = SystemContext.context().getBeansByPriority(Somei.class, Priority.class);
    assertThat(beans).hasSize(3);

    assertThat(beans.get(0)).isInstanceOf(BSomei.class);
    assertThat(beans.get(1)).isInstanceOf(ASomei.class);
    assertThat(beans.get(2)).isInstanceOf(A2Somei.class);
  }

  @Test
  public void getBeansUnsorted_withPriority() {

    final List<Somei> beans = SystemContext.getBeans(Somei.class);
    assertThat(beans).hasSize(3);
    // can't assert bean order
  }

  @Test
  public void getBeansWithAnnotation() {

    final List<Object> beans = SystemContext.getBeansWithAnnotation(Fruit.class);
    assertThat(beans).hasSize(2);
  }
}
