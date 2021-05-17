package io.avaje.inject;

import io.avaje.inject.spi.BeanScopeFactory;
import io.avaje.inject.spi.Builder;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class BeanScopeBuilderTest {

  @Test
  public void noDepends() {

    DBeanScopeBuilder.FactoryOrder factoryOrder = new DBeanScopeBuilder.FactoryOrder(Collections.emptySet(), true, true);
    factoryOrder.add(bc("1", null, null));
    factoryOrder.add(bc("2", null, null));
    factoryOrder.add(bc("3", null, null));
    factoryOrder.orderFactories();

    assertThat(names(factoryOrder.factories())).containsExactly("1", "2", "3");
  }

  @Test
  public void name_depends() {

    DBeanScopeBuilder.FactoryOrder factoryOrder = new DBeanScopeBuilder.FactoryOrder(Collections.emptySet(), true, true);
    factoryOrder.add(bc("two", null, "one"));
    factoryOrder.add(bc("one", null, null));
    factoryOrder.orderFactories();

    assertThat(names(factoryOrder.factories())).containsExactly("one", "two");
  }

  @Test
  public void name_depends4() {

    DBeanScopeBuilder.FactoryOrder factoryOrder = new DBeanScopeBuilder.FactoryOrder(Collections.emptySet(), true, true);
    factoryOrder.add(bc("1", null, "3"));
    factoryOrder.add(bc("2", null, "4"));
    factoryOrder.add(bc("3", null, "4"));
    factoryOrder.add(bc("4", null, null));

    factoryOrder.orderFactories();

    assertThat(names(factoryOrder.factories())).containsExactly("4", "2", "3", "1");
  }

  @Test
  public void nameFeature_depends() {

    DBeanScopeBuilder.FactoryOrder factoryOrder = new DBeanScopeBuilder.FactoryOrder(Collections.emptySet(), true, true);
    factoryOrder.add(bc("1", "a", "3"));
    factoryOrder.add(bc("2", null, "4,a"));
    factoryOrder.add(bc("3", null, "4"));
    factoryOrder.add(bc("4", null, null));

    factoryOrder.orderFactories();

    assertThat(names(factoryOrder.factories())).containsExactly("4", "3", "1", "2");
  }

  @Test
  public void feature_depends() {

    DBeanScopeBuilder.FactoryOrder factoryOrder = new DBeanScopeBuilder.FactoryOrder(Collections.emptySet(), true, true);
    factoryOrder.add(bc("two", null, "myfeature"));
    factoryOrder.add(bc("one", "myfeature", null));
    factoryOrder.orderFactories();

    assertThat(names(factoryOrder.factories())).containsExactly("one", "two");
  }

  @Test
  public void feature_depends2() {

    DBeanScopeBuilder.FactoryOrder factoryOrder = new DBeanScopeBuilder.FactoryOrder(Collections.emptySet(), true, true);
    factoryOrder.add(bc("two", null, "myfeature"));
    factoryOrder.add(bc("one", "myfeature", null));
    factoryOrder.add(bc("three", "myfeature", null));
    factoryOrder.orderFactories();

    assertThat(names(factoryOrder.factories())).containsExactly("one", "three", "two");
  }

  private List<String> names(List<BeanScopeFactory> factories) {
    return factories.stream()
      .map(BeanScopeFactory::getName)
      .collect(Collectors.toList());
  }

  private TDBeanScope bc(String name, String provides, String dependsOn) {
    return new TDBeanScope(name, split(provides), split(dependsOn));
  }

  private String[] split(String val) {
    return val == null ? null : val.split(",");
  }

  private static class TDBeanScope implements BeanScopeFactory {

    final String name;
    final String[] provides;
    final String[] dependsOn;

    private TDBeanScope(String name, String[] provides, String[] dependsOn) {
      this.name = name;
      this.provides = provides;
      this.dependsOn = dependsOn;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public String[] getProvides() {
      return provides;
    }

    @Override
    public String[] getDependsOn() {
      return dependsOn;
    }

    @Override
    public void build(Builder parent) {

    }
  }
}
