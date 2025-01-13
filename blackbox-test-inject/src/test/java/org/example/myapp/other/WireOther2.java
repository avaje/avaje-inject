package org.example.myapp.other;

import io.avaje.inject.PostConstruct;
import jakarta.inject.Singleton;
import org.example.external.aspect.PluginProvidedClass;
import org.other.one.OtherComponent;

@Singleton
public class WireOther2 {
  OtherComponent component;
  PluginProvidedClass plugin;

  @PostConstruct
  public void postConstruct(OtherComponent component, PluginProvidedClass plugin) {
    this.component = component;
    this.plugin = plugin;
  }

}
