package org.example.coffee;

import io.dinject.BeanContext;
import io.dinject.BeanContextBuilder;

import java.util.List;

class ExtensionExample {

  private final List<Class<?>> withMocks;
  private final List<Class> withSpies;

  ExtensionExample(List<Class<?>> withMocks, List<Class> withSpies) {
    this.withMocks = withMocks;
    this.withSpies = withSpies;
  }

  BeanContext build() {
    BeanContextBuilder bootContext = new BeanContextBuilder();
    withMocks.forEach(bootContext::withMock);
    withSpies.forEach(bootContext::withSpy);
    return bootContext.build();
  }

}
