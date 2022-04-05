package io.avaje.inject;

import io.avaje.inject.spi.Module;
import io.avaje.inject.spi.Builder;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

  @Test
  public void name_requiresPackage() {
    DBeanScopeBuilder.FactoryOrder factoryOrder = new DBeanScopeBuilder.FactoryOrder(Collections.emptySet(), true);
    factoryOrder.add(bc("1", null, new Class[0], of(Mod3.class)));
    factoryOrder.add(bc("2", null, new Class[0], of(Mod4.class)));
    factoryOrder.add(bc("3", of(Mod3.class), new Class[0], of(Mod4.class)));
    factoryOrder.add(bc("4", of(Mod4.class), new Class[0]));

    factoryOrder.orderFactories();

    assertThat(names(factoryOrder.factories())).containsExactly("4", "2", "3", "1");
  }

  @Test
  public void name_requiresPackage_mixed() {
    DBeanScopeBuilder.FactoryOrder factoryOrder = new DBeanScopeBuilder.FactoryOrder(Collections.emptySet(), true);
    factoryOrder.add(bc("1", null, new Class[0], of(Mod3.class)));
    factoryOrder.add(bc("2", null, of(Mod4.class), new Class[0]));
    factoryOrder.add(bc("3", of(Mod3.class), new Class[0], of(Mod4.class)));
    factoryOrder.add(bc("4", of(Mod4.class), new Class[0]));

    factoryOrder.orderFactories();

    assertThat(names(factoryOrder.factories())).containsExactly("4", "2", "3", "1");
  }

  @Test
  public void missingRequiresPackage_expect_unsatisfiedRequiresPackages() {
    DBeanScopeBuilder.FactoryOrder factoryOrder = new DBeanScopeBuilder.FactoryOrder(Collections.emptySet(), false);
    factoryOrder.add(bc("1", null, new Class[0], of(Mod3.class)));
    factoryOrder.add(bc("2", null, of(Mod4.class), new Class[0]));
    factoryOrder.add(bc("4", of(Mod4.class), new Class[0]));

    assertThatThrownBy(factoryOrder::orderFactories)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("has unsatisfied requiresPackages [io.avaje.inject.BeanScopeBuilderTest$Mod3] ");
  }

  @Test
  public void missingRequires_expect_unsatisfiedRequires() {
    DBeanScopeBuilder.FactoryOrder factoryOrder = new DBeanScopeBuilder.FactoryOrder(Collections.emptySet(), false);
    factoryOrder.add(bc("1", null, of(Mod3.class), new Class[0]));
    factoryOrder.add(bc("2", null, of(Mod4.class), new Class[0]));
    factoryOrder.add(bc("4", of(Mod4.class), new Class[0]));

    assertThatThrownBy(factoryOrder::orderFactories)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("has unsatisfied requires [io.avaje.inject.BeanScopeBuilderTest$Mod3] ");
  }

  private List<String> names(List<Module> factories) {
    return factories.stream()
      .map(Module::toString)
      .collect(Collectors.toList());
  }

  private TDBeanScope bc(String name, Class<?>[] provides, Class<?>[] requires) {
    return bc(name, provides, requires, new Class[0]);
  }

  private TDBeanScope bc(String name, Class<?>[] provides, Class<?>[] requires, Class<?>[] requiresPkg) {
    return new TDBeanScope(name, provides, requires, requiresPkg);
  }

  private static class TDBeanScope implements Module {

    final String name;
    final Class<?>[] provides;
    final Class<?>[] requires;
    final Class<?>[] requiresPackages;

    private TDBeanScope(String name, Class<?>[] provides, Class<?>[] requires, Class<?>[] requiresPackages) {
      this.name = name;
      this.provides = provides;
      this.requires = requires;
      this.requiresPackages = requiresPackages;
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
    public Class<?>[] classes() {
      return new Class[0];
    }

    @Override
    public Class<?>[] requires() {
      return requires;
    }

    @Override
    public Class<?>[] requiresPackages() {
      return requiresPackages;
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
