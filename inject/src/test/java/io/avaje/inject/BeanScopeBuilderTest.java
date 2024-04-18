package io.avaje.inject;

import static io.avaje.inject.spi.Module.EMPTY_CLASSES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.GenericType;
import io.avaje.inject.spi.Module;

@SuppressWarnings("all")
class BeanScopeBuilderTest {

  @Test
  void depends_providedByParent() {
    DBeanScopeBuilder.FactoryOrder factoryOrder = new DBeanScopeBuilder.FactoryOrder(new TDBeanScope(MyFeature.class), Collections.emptySet(), false);
    factoryOrder.add(bc("1", EMPTY_CLASSES, of(MyFeature.class)));
    factoryOrder.orderModules();

    assertThat(names(factoryOrder.factories())).containsExactly("1");
  }

  @Test
  void depends_notProvidedByParent_expect_IllegalStateException() {
    DBeanScopeBuilder.FactoryOrder factoryOrder = new DBeanScopeBuilder.FactoryOrder(new TDBeanScope(FeatureA.class), Collections.emptySet(), false);
    factoryOrder.add(bc("1", EMPTY_CLASSES, of(MyFeature.class)));
    assertThatThrownBy(factoryOrder::orderModules)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Module [io.avaje.inject.BeanScopeBuilderTest$TDModule] has unsatisfied requires [io.avaje.inject.BeanScopeBuilderTest$MyFeature]");
  }

  @Test
  void noDepends() {
    DBeanScopeBuilder.FactoryOrder factoryOrder = new DBeanScopeBuilder.FactoryOrder(null, Collections.emptySet(), true);
    factoryOrder.add(bc("1", EMPTY_CLASSES, EMPTY_CLASSES));
    factoryOrder.add(bc("2", EMPTY_CLASSES, EMPTY_CLASSES));
    factoryOrder.add(bc("3", EMPTY_CLASSES, EMPTY_CLASSES));
    factoryOrder.orderModules();

    assertThat(names(factoryOrder.factories())).containsExactly("1", "2", "3");
  }

  @Test
  void providedFirst() {
    DBeanScopeBuilder.FactoryOrder factoryOrder = new DBeanScopeBuilder.FactoryOrder(null, Collections.emptySet(), true);
    factoryOrder.add(bc("two", EMPTY_CLASSES, EMPTY_CLASSES));
    factoryOrder.add(bc("one", of(Mod3.class), EMPTY_CLASSES));
    factoryOrder.orderModules();

    assertThat(names(factoryOrder.factories())).containsExactly("one", "two");
  }

  @Test
  void name_depends() {
    DBeanScopeBuilder.FactoryOrder factoryOrder = new DBeanScopeBuilder.FactoryOrder(null, Collections.emptySet(), true);
    factoryOrder.add(bc("two", EMPTY_CLASSES, of(Mod3.class)));
    factoryOrder.add(bc("one", EMPTY_CLASSES, EMPTY_CLASSES));
    factoryOrder.orderModules();

    assertThat(names(factoryOrder.factories())).containsExactly("one", "two");
  }

  @Test
  void name_depends4() {
    DBeanScopeBuilder.FactoryOrder factoryOrder = new DBeanScopeBuilder.FactoryOrder(null, Collections.emptySet(), true);
    factoryOrder.add(bc("1", EMPTY_CLASSES, of(Mod3.class)));
    factoryOrder.add(bc("2", EMPTY_CLASSES, of(Mod4.class)));
    factoryOrder.add(bc("3", of(Mod3.class), of(Mod4.class)));
    factoryOrder.add(bc("4", of(Mod4.class), null));

    factoryOrder.orderModules();

    assertThat(names(factoryOrder.factories())).containsExactly("4", "2", "3", "1");
  }

  @Test
  void nameFeature_depends() {
    DBeanScopeBuilder.FactoryOrder factoryOrder = new DBeanScopeBuilder.FactoryOrder(null, Collections.emptySet(), true);
    factoryOrder.add(bc("1", of(FeatureA.class), of(Mod3.class)));
    factoryOrder.add(bc("2", EMPTY_CLASSES, of(Mod4.class, FeatureA.class)));
    factoryOrder.add(bc("3", of(Mod3.class), of(Mod4.class)));
    factoryOrder.add(bc("4", of(Mod4.class), null));

    factoryOrder.orderModules();

    assertThat(names(factoryOrder.factories())).containsExactly("4", "3", "1", "2");
  }

  @Test
  void feature_depends() {
    DBeanScopeBuilder.FactoryOrder factoryOrder = new DBeanScopeBuilder.FactoryOrder(null, Collections.emptySet(), true);
    factoryOrder.add(bc("two", EMPTY_CLASSES, of(MyFeature.class)));
    factoryOrder.add(bc("one", of(MyFeature.class), null));
    factoryOrder.orderModules();

    assertThat(names(factoryOrder.factories())).containsExactly("one", "two");
  }

  @Test
  void feature_depends_generic() {
    DBeanScopeBuilder.FactoryOrder factoryOrder = new DBeanScopeBuilder.FactoryOrder(null, Collections.emptySet(), true);
    factoryOrder.add(bc("two", EMPTY_CLASSES, of(new GenericType<Map<String, MyFeature>>() {})));
    factoryOrder.add(bc("one", of(new GenericType<Map<String, MyFeature>>() {}), EMPTY_CLASSES));
    factoryOrder.add(bc("three", of(new GenericType<Map<String, MyFeature>>() {}), EMPTY_CLASSES));
    factoryOrder.orderModules();

    assertThat(names(factoryOrder.factories())).containsExactly("one", "three", "two");
  }


  @Test
  void feature_depends2() {
    DBeanScopeBuilder.FactoryOrder factoryOrder = new DBeanScopeBuilder.FactoryOrder(null, Collections.emptySet(), true);
    factoryOrder.add(bc("two", EMPTY_CLASSES, of(MyFeature.class)));
    factoryOrder.add(bc("one", of(MyFeature.class), EMPTY_CLASSES));
    factoryOrder.add(bc("three", of(MyFeature.class), EMPTY_CLASSES));
    factoryOrder.orderModules();

    assertThat(names(factoryOrder.factories())).containsExactly("one", "three", "two");
  }

  @Test
  void name_requiresPackage() {
    DBeanScopeBuilder.FactoryOrder factoryOrder = new DBeanScopeBuilder.FactoryOrder(null, Collections.emptySet(), true);
    factoryOrder.add(bc("1", EMPTY_CLASSES, new Class[0], of(Mod3.class)));
    factoryOrder.add(bc("2", EMPTY_CLASSES, new Class[0], of(Mod4.class)));
    factoryOrder.add(bc("3", of(Mod3.class), new Class[0], of(Mod4.class)));
    factoryOrder.add(bc("4", of(Mod4.class), new Class[0]));

    factoryOrder.orderModules();

    assertThat(names(factoryOrder.factories())).containsExactly("4", "2", "3", "1");
  }

  @Test
  void name_requiresPackage_mixed() {
    DBeanScopeBuilder.FactoryOrder factoryOrder = new DBeanScopeBuilder.FactoryOrder(null, Collections.emptySet(), true);
    factoryOrder.add(bc("1", EMPTY_CLASSES, new Class[0], of(Mod3.class)));
    factoryOrder.add(bc("2", EMPTY_CLASSES, of(Mod4.class), new Class[0]));
    factoryOrder.add(bc("3", of(Mod3.class), new Class[0], of(Mod4.class)));
    factoryOrder.add(bc("4", of(Mod4.class), new Class[0]));

    factoryOrder.orderModules();

    assertThat(names(factoryOrder.factories())).containsExactly("4", "2", "3", "1");
  }

  @Test
  void missingRequiresPackage_expect_unsatisfiedRequiresPackages() {
    DBeanScopeBuilder.FactoryOrder factoryOrder = new DBeanScopeBuilder.FactoryOrder(null, Collections.emptySet(), false);
    factoryOrder.add(bc("1", EMPTY_CLASSES, new Class[0], of(Mod3.class)));
    factoryOrder.add(bc("2", EMPTY_CLASSES, of(Mod4.class), new Class[0]));
    factoryOrder.add(bc("4", of(Mod4.class), new Class[0]));

    assertThatThrownBy(factoryOrder::orderModules)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("has unsatisfied requiresPackages [io.avaje.inject.BeanScopeBuilderTest$Mod3] ");
  }

  @Test
  void missingRequires_expect_unsatisfiedRequires() {
    DBeanScopeBuilder.FactoryOrder factoryOrder = new DBeanScopeBuilder.FactoryOrder(null, Collections.emptySet(), false);
    factoryOrder.add(bc("1", EMPTY_CLASSES, of(Mod3.class), new Class[0]));
    factoryOrder.add(bc("2", EMPTY_CLASSES, of(Mod4.class), new Class[0]));
    factoryOrder.add(bc("4", of(Mod4.class), new Class[0]));

    assertThatThrownBy(factoryOrder::orderModules)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("has unsatisfied requires [io.avaje.inject.BeanScopeBuilderTest$Mod3] ");
  }

  private List<String> names(List<Module> factories) {
    return factories.stream()
      .map(Module::toString)
      .collect(Collectors.toList());
  }

  private TDModule bc(String name, Type[] provides, Type[] requires) {
    return bc(name, provides, requires, new Class[0]);
  }

  private TDModule bc(String name, Type[] provides, Type[] requires, Type[] requiresPkg) {
    return new TDModule(name, provides, requires, requiresPkg);
  }

  private static class TDModule implements Module {

    final String name;
    final Type[] provides;
    final Type[] requires;
    final Type[] requiresPackages;

    private TDModule(String name, Type[] provides, Type[] requires, Type[] requiresPackages) {
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
    public Type[] provides() {
      return provides;
    }

    @Override
    public Class<?>[] classes() {
      return new Class[0];
    }

    @Override
    public Type[] requires() {
      return requires;
    }

    @Override
    public Type[] requiresPackages() {
      return requiresPackages;
    }

    @Override
    public void build(Builder parent) {

    }
  }

  Type[] of(Type... cls) {
    return cls != null ? cls : new Type[0];
  }

  class MyFeature {
  }
  class FeatureA {
  }
  class Mod3 {
  }
  class Mod4 {
  }

  static class TDBeanScope implements BeanScope {

    final String containsType;

    TDBeanScope(Type containsType) {
      this.containsType = containsType.getTypeName();
    }

    @Override
    public boolean contains(String type) {
      return containsType.equals(type);
    }

    @Override
    public <T> T get(Class<T> type) {
      return null;
    }

    @Override
    public <T> T get(Class<T> type, String name) {
      return null;
    }

    @Override
    public <T> T get(Type type, String name) {
      return null;
    }

    @Override
    public <T> Optional<T> getOptional(Class<T> type) {
      return Optional.empty();
    }

    @Override
    public <T> Optional<T> getOptional(Type type, String name) {
      return Optional.empty();
    }

    @Override
    public List<Object> listByAnnotation(Class<? extends Annotation> annotation) {
      return null;
    }

    @Override
    public <T> List<T> list(Class<T> type) {
      return null;
    }

    @Override
    public <T> List<T> list(Type type) {
      return null;
    }

    @Override
    public <T> List<T> listByPriority(Class<T> type) {
      return null;
    }

    @Override
    public <T> List<T> listByPriority(Class<T> type, Class<? extends Annotation> priority) {
      return null;
    }

    @Override
    public <T> Map<String, T> map(Type type) {
      return Collections.emptyMap();
    }

    @Override
    public List<BeanEntry> all() {
      return null;
    }

    @Override
    public boolean contains(Type type) {
      return false;
    }

    @Override
    public void close() {

    }
  }
}
