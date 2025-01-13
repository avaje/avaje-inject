package org.example.myapp.other;

import io.avaje.inject.BeanScope;
import io.avaje.inject.PostConstruct;
import jakarta.inject.Singleton;
import org.example.external.aspect.PluginProvidedClass;
import org.other.one.OtherComponent;

@Singleton
public class WireOther3 {
  BeanScope beanScope;

  @PostConstruct
  public void postConstruct(BeanScope beanScope) {
    this.beanScope = beanScope;
  }

}
