package org.example.myapp.other;

import org.example.external.aspect.PluginProvidedClass;
import org.other.one.OtherComponent;

import jakarta.inject.Singleton;

@Singleton
public class WireOther {
  OtherComponent component;
  PluginProvidedClass plugin;

  public WireOther(OtherComponent component, PluginProvidedClass plugin) {
    this.component = component;
    this.plugin = plugin;
  }
}
