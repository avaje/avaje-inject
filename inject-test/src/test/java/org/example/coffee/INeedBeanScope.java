package org.example.coffee;

import io.avaje.inject.BeanScope;
import io.avaje.inject.Component;
import org.example.optional.Que;

/**
 * Depends on the BeanScope.
 */
@Component
class INeedBeanScope {

  final BeanScope beanScope;

  INeedBeanScope(BeanScope beanScope) {
    this.beanScope = beanScope;
  }

  Que getQueue() {
    return beanScope.get(Que.class, "Frodo");
  }
}
