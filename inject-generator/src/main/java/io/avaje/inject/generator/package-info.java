@io.avaje.prism.GeneratePrisms({
  @GeneratePrism(value = InjectModule.class),
  @GeneratePrism(value = Factory.class),
  @GeneratePrism(value = Singleton.class),
  @GeneratePrism(value = Component.class),
  @GeneratePrism(value = Prototype.class),
  @GeneratePrism(value = Scope.class),
  @GeneratePrism(value = Qualifier.class),
  @GeneratePrism(value = Named.class),
  @GeneratePrism(value = Inject.class),
  @GeneratePrism(value = Aspect.class),
  @GeneratePrism(value = Primary.class),
  @GeneratePrism(value = Secondary.class),
  @GeneratePrism(value = Proxy.class),
  @GeneratePrism(value = DependencyMeta.class),
  @GeneratePrism(value = Bean.class),
  @GeneratePrism(value = io.avaje.inject.spi.Generated.class),
})
package io.avaje.inject.generator;

import io.avaje.inject.Bean;
import io.avaje.inject.Component;
import io.avaje.inject.Factory;
import io.avaje.inject.InjectModule;
import io.avaje.inject.Primary;
import io.avaje.inject.Prototype;
import io.avaje.inject.Secondary;
import io.avaje.inject.aop.Aspect;
import io.avaje.inject.spi.DependencyMeta;
import io.avaje.inject.spi.Proxy;
import io.avaje.prism.GeneratePrism;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Qualifier;
import jakarta.inject.Scope;
import jakarta.inject.Singleton;
