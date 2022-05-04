package io.avaje.inject.xtra;

import io.avaje.inject.BeanScope;
import org.example.coffee.fruit.Fruit;
import org.example.coffee.list.A2Somei;
import org.example.coffee.list.ASomei;
import org.example.coffee.list.BSomei;
import org.example.coffee.list.Somei;
import org.example.coffee.priority.base.ABasei;
import org.example.coffee.priority.base.BBasei;
import org.example.coffee.priority.base.BaseIface;
import org.example.coffee.priority.base.CBasei;
import org.junit.jupiter.api.Test;

import javax.annotation.Priority;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SystemContextTest {

  @Test
  public void getBeansByPriority() {
    try (BeanScope context = BeanScope.builder().build()) {
      final List<BaseIface> beans = context.listByPriority(BaseIface.class);
      assertThat(beans).hasSize(3);

      assertThat(beans.get(0)).isInstanceOf(CBasei.class);
      assertThat(beans.get(1)).isInstanceOf(BBasei.class);
      assertThat(beans.get(2)).isInstanceOf(ABasei.class);
    }
  }

  @Test
  public void getBeansByPriority_withAnnotation() {
    try (BeanScope context = BeanScope.builder().build()) {
      final List<Somei> beans = context.listByPriority(Somei.class, Priority.class);
      assertThat(beans).hasSize(3);

      assertThat(beans.get(0)).isInstanceOf(BSomei.class);
      assertThat(beans.get(1)).isInstanceOf(ASomei.class);
      assertThat(beans.get(2)).isInstanceOf(A2Somei.class);
    }
  }

  @Test
  public void getBeansUnsorted_withPriority() {
    try (BeanScope context = BeanScope.builder().build()) {
      final List<Somei> beans = context.list(Somei.class);
      assertThat(beans).hasSize(3);
      // can't assert bean order
    }
  }

  @Test
  public void getBeansWithAnnotation() {
    try (BeanScope context = BeanScope.builder().build()) {
      final List<Object> beans = context.listByAnnotation(Fruit.class);
      assertThat(beans).hasSize(2);
    }
  }
}
