package io.dinject;

import io.dinject.core.BeanContextFactory;
import io.dinject.core.Builder;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class BootContextTest {

  @Test
  public void noDepends() {

    BootContext.FactoryOrder factoryOrder = new BootContext.FactoryOrder(Collections.emptySet(), true, true);
    factoryOrder.add(bc("1", null, null));
    factoryOrder.add(bc("2", null, null));
    factoryOrder.add(bc("3", null, null));
    factoryOrder.orderFactories();

    assertThat(names(factoryOrder.factories())).containsExactly("1", "2", "3");
  }

  @Test
  public void name_depends() {

    BootContext.FactoryOrder factoryOrder = new BootContext.FactoryOrder(Collections.emptySet(), true, true);
    factoryOrder.add(bc("two", null, "one"));
    factoryOrder.add(bc("one", null, null));
    factoryOrder.orderFactories();

    assertThat(names(factoryOrder.factories())).containsExactly("one", "two");
  }

  @Test
  public void name_depends4() {

    BootContext.FactoryOrder factoryOrder = new BootContext.FactoryOrder(Collections.emptySet(), true, true);
    factoryOrder.add(bc("1", null, "3"));
    factoryOrder.add(bc("2", null, "4"));
    factoryOrder.add(bc("3", null, "4"));
    factoryOrder.add(bc("4", null, null));

    factoryOrder.orderFactories();

    assertThat(names(factoryOrder.factories())).containsExactly("4", "2", "3", "1");
  }

  @Test
  public void nameFeature_depends() {

    BootContext.FactoryOrder factoryOrder = new BootContext.FactoryOrder(Collections.emptySet(), true, true);
    factoryOrder.add(bc("1", "a", "3"));
    factoryOrder.add(bc("2", null, "4,a"));
    factoryOrder.add(bc("3", null, "4"));
    factoryOrder.add(bc("4", null, null));

    factoryOrder.orderFactories();

    assertThat(names(factoryOrder.factories())).containsExactly("4", "3", "1", "2");
  }

  @Test
  public void feature_depends() {

    BootContext.FactoryOrder factoryOrder = new BootContext.FactoryOrder(Collections.emptySet(), true, true);
    factoryOrder.add(bc("two", null, "myfeature"));
    factoryOrder.add(bc("one", "myfeature", null));
    factoryOrder.orderFactories();

    assertThat(names(factoryOrder.factories())).containsExactly("one", "two");
  }

  @Test
  public void feature_depends2() {

    BootContext.FactoryOrder factoryOrder = new BootContext.FactoryOrder(Collections.emptySet(), true, true);
    factoryOrder.add(bc("two", null, "myfeature"));
    factoryOrder.add(bc("one", "myfeature", null));
    factoryOrder.add(bc("three", "myfeature", null));
    factoryOrder.orderFactories();

    assertThat(names(factoryOrder.factories())).containsExactly("one", "three", "two");
  }

  private List<String> names(List<BeanContextFactory> factories) {
    return factories.stream()
      .map(BeanContextFactory::getName)
      .collect(Collectors.toList());
  }

  private TDBeanContext bc(String name, String provides, String dependsOn) {
    return new TDBeanContext(name, split(provides), split(dependsOn));
  }

  private String[] split(String val) {
    return val == null ? null : val.split(",");
  }

  private static class TDBeanContext implements BeanContextFactory {

    final String name;
    final String[] provides;
    final String[] dependsOn;

    private TDBeanContext(String name, String[] provides, String[] dependsOn) {
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
    public BeanContext createContext(Builder parent) {
      return null;
    }
  }
}
