package org.example.external.aspect.spi;

import org.example.external.aspect.PluginProvidedClass;

import io.avaje.inject.BeanScopeBuilder;
import io.avaje.inject.spi.InjectPlugin;
import io.avaje.inject.spi.PluginProvides;

@PluginProvides(provides = PluginProvidedClass.class)
public class AspectPlugin implements InjectPlugin {

  @Override
  public Class<?>[] provides() {
    return new Class<?>[] {PluginProvidedClass.class};
  }

  @Override
  public void apply(BeanScopeBuilder builder) {
    builder.beans(new PluginProvidedClass());
  }
}
