package org.example.coffee;

import io.avaje.inject.BeanScope;
import io.avaje.inject.BeanScopeBuilder;

import java.util.List;

class ExtensionExample {

  private final List<Class<?>> mocks;
  private final List<Class> spies;

  ExtensionExample(List<Class<?>> mocks, List<Class> spies) {
    this.mocks = mocks;
    this.spies = spies;
  }

  BeanScope build() {
    BeanScopeBuilder.ForTesting bootContext = BeanScope.builder().forTesting();
    mocks.forEach(bootContext::mock);
    spies.forEach(bootContext::spy);
    return bootContext.build();
  }

}
