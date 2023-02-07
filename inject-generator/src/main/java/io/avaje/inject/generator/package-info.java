@net.java.dev.hickory.prism.GeneratePrisms({
  @GeneratePrism(value = InjectModule.class, publicAccess = true),
  @GeneratePrism(value = Factory.class, publicAccess = true),
  @GeneratePrism(value = Singleton.class, publicAccess = true),
  @GeneratePrism(value = Component.class, publicAccess = true),
  @GeneratePrism(value = Prototype.class, publicAccess = true),
  @GeneratePrism(value = Scope.class, publicAccess = true),
})
package io.avaje.inject.generator;

import io.avaje.inject.Component;
import io.avaje.inject.Factory;
import io.avaje.inject.InjectModule;
import io.avaje.inject.Prototype;
import jakarta.inject.Scope;
import jakarta.inject.Singleton;
import net.java.dev.hickory.prism.GeneratePrism;
