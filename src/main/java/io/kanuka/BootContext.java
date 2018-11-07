package io.kanuka;

import io.kanuka.core.BeanContextFactory;
import io.kanuka.core.Builder;
import io.kanuka.core.BuilderFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class BootContext {

  /**
   * Load all the named contexts.
   */
  public BeanContext load() {

    List<BeanContextFactory> factories = new ArrayList<>();
    ServiceLoader.load(BeanContextFactory.class).forEach(factories::add);

    // sort factories by dependsOn

    Builder rootBuilder = BuilderFactory.newRootBuilder();

    for (BeanContextFactory factory : factories) {
      rootBuilder.addChild(factory.createContext(rootBuilder));
    }

    // entire graph built, fire postConstruct
    rootBuilder.postConstruct();
    return rootBuilder.build();
  }

}
