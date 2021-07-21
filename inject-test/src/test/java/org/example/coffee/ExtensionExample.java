package org.example.coffee;

import io.avaje.inject.BeanScope;
import io.avaje.inject.BeanScopeBuilder;

import java.util.List;

class ExtensionExample {

  private final List<Class<?>> withMocks;
  private final List<Class> withSpies;

  ExtensionExample(List<Class<?>> withMocks, List<Class> withSpies) {
    this.withMocks = withMocks;
    this.withSpies = withSpies;
  }

  BeanScope build() {
    BeanScopeBuilder.ForTesting bootContext = BeanScope.newBuilder().forTesting();
    withMocks.forEach(bootContext::withMock);
    withSpies.forEach(bootContext::withSpy);
    return bootContext.build();
  }

}
