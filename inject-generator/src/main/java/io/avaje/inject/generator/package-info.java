@net.java.dev.hickory.prism.GeneratePrisms({
  @GeneratePrism(value = InjectModule.class, publicAccess = true),
  @GeneratePrism(value = Factory.class, publicAccess = true),
  @GeneratePrism(value = Singleton.class, publicAccess = true),
  @GeneratePrism(value = Component.class, publicAccess = true),
  @GeneratePrism(value = Prototype.class, publicAccess = true),
  @GeneratePrism(value = Scope.class, publicAccess = true),
  @GeneratePrism(value = Qualifier.class, publicAccess = true),
  @GeneratePrism(value = Named.class, publicAccess = true),
  @GeneratePrism(value = Inject.class, publicAccess = true),
  @GeneratePrism(value = Aspect.class, publicAccess = true),
  @GeneratePrism(value = Primary.class, publicAccess = true),
  @GeneratePrism(value = Secondary.class, publicAccess = true),
  @GeneratePrism(value = Proxy.class, publicAccess = true),
  @GeneratePrism(value = DependencyMeta.class, publicAccess = true),
  @GeneratePrism(value = Bean.class, publicAccess = true),
  @GeneratePrism(value = io.avaje.inject.spi.Generated.class, publicAccess = true),

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
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Qualifier;
import jakarta.inject.Scope;
import jakarta.inject.Singleton;
import net.java.dev.hickory.prism.GeneratePrism;
