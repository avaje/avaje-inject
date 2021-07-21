package io.avaje.inject;

import io.avaje.inject.spi.Module;
import io.avaje.inject.spi.Builder;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class BeanScopeBuilderTest {

  @Test
  public void noDepends() {

    DBeanScopeBuilder.FactoryOrder factoryOrder = new DBeanScopeBuilder.FactoryOrder(Collections.emptySet(), true);
    factoryOrder.add(bc("1", null, null));
    factoryOrder.add(bc("2", null, null));
    factoryOrder.add(bc("3", null, null));
    factoryOrder.orderFactories();

    assertThat(names(factoryOrder.factories())).containsExactly("1", "2", "3");
  }

  @Test
  public void name_depends() {

    DBeanScopeBuilder.FactoryOrder factoryOrder = new DBeanScopeBuilder.FactoryOrder(Collections.emptySet(), true);
    factoryOrder.add(bc("two", null, of(Mod3.class)));
    factoryOrder.add(bc("one", null, null));
    factoryOrder.orderFactories();

    assertThat(names(factoryOrder.factories())).containsExactly("one", "two");
  }

  @Test
  public void name_depends4() {

    DBeanScopeBuilder.FactoryOrder factoryOrder = new DBeanScopeBuilder.FactoryOrder(Collections.emptySet(), true);
    factoryOrder.add(bc("1", null, of(Mod3.class)));
    factoryOrder.add(bc("2", null, of(Mod4.class)));
    factoryOrder.add(bc("3", of(Mod3.class), of(Mod4.class)));
    factoryOrder.add(bc("4", of(Mod4.class), null));

    factoryOrder.orderFactories();

    assertThat(names(factoryOrder.factories())).containsExactly("4", "2", "3", "1");
  }

  @Test
  public void nameFeature_depends() {

    DBeanScopeBuilder.FactoryOrder factoryOrder = new DBeanScopeBuilder.FactoryOrder(Collections.emptySet(), true);
    factoryOrder.add(bc("1", of(FeatureA.class), of(Mod3.class)));
    factoryOrder.add(bc("2", null, of(Mod4.class, FeatureA.class)));
    factoryOrder.add(bc("3", of(Mod3.class), of(Mod4.class)));
    factoryOrder.add(bc("4", of(Mod4.class), null));

    factoryOrder.orderFactories();

    assertThat(names(factoryOrder.factories())).containsExactly("4", "3", "1", "2");
  }

  @Test
  public void feature_depends() {

    DBeanScopeBuilder.FactoryOrder factoryOrder = new DBeanScopeBuilder.FactoryOrder(Collections.emptySet(), true);
    factoryOrder.add(bc("two", null, of(MyFeature.class)));
    factoryOrder.add(bc("one", of(MyFeature.class), null));
    factoryOrder.orderFactories();

    assertThat(names(factoryOrder.factories())).containsExactly("one", "two");
  }

  @Test
  public void feature_depends2() {

    DBeanScopeBuilder.FactoryOrder factoryOrder = new DBeanScopeBuilder.FactoryOrder(Collections.emptySet(), true);
    factoryOrder.add(bc("two", null, of(MyFeature.class)));
    factoryOrder.add(bc("one", of(MyFeature.class), null));
    factoryOrder.add(bc("three", of(MyFeature.class), null));
    factoryOrder.orderFactories();

    assertThat(names(factoryOrder.factories())).containsExactly("one", "three", "two");
  }

  private List<String> names(List<Module> factories) {
    return factories.stream()
      .map(Module::toString)
      .collect(Collectors.toList());
  }

  private TDBeanScope bc(String name, Class<?>[] provides, Class<?>[] dependsOn) {
    return new TDBeanScope(name, provides, dependsOn);
  }

  private static class TDBeanScope implements Module {

    final String name;
    final Class<?>[] provides;
    final Class<?>[] requires;

    private TDBeanScope(String name, Class<?>[] provides, Class<?>[] requires) {
      this.name = name;
      this.provides = provides;
      this.requires = requires;
    }

    @Override
    public String toString() {
      return name;
    }

    @Override
    public Class<?>[] provides() {
      return provides;
    }

    @Override
    public Class<?>[] requires() {
      return requires;
    }

    @Override
    public void build(Builder parent) {

    }
  }

  Class<?>[] of(Class<?>... cls) {
    return cls != null ? cls : new Class<?>[0];
  }

  class MyFeature {
  }
  class FeatureA {
  }
  class Mod3 {
  }
  class Mod4 {
  }
}
