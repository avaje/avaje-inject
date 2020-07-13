package org.example.coffee;

import io.dinject.BeanContext;
import io.dinject.BootContext;

import java.util.List;

class ExtensionExample {

  private final List<Class<?>> withMocks;
  private final List<Class> withSpies;

  ExtensionExample(List<Class<?>> withMocks, List<Class> withSpies) {
    this.withMocks = withMocks;
    this.withSpies = withSpies;
  }

  BeanContext build() {

    BootContext bootContext = new BootContext();

    withMocks.forEach(bootContext::withMock);
    withSpies.forEach(bootContext::withSpy);

    return bootContext.load();
  }

}
