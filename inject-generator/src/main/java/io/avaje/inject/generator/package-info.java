@GeneratePrism(InjectModule.class)
@GeneratePrism(Factory.class)
@GeneratePrism(Singleton.class)
@GeneratePrism(Component.class)
@GeneratePrism(Component.Import.class)
@GeneratePrism(Prototype.class)
@GeneratePrism(Scope.class)
@GeneratePrism(Qualifier.class)
@GeneratePrism(Named.class)
@GeneratePrism(Inject.class)
@GeneratePrism(Aspect.class)
@GeneratePrism(value = Aspect.Import.class, name = "AspectImportPrism")
@GeneratePrism(Primary.class)
@GeneratePrism(Secondary.class)
@GeneratePrism(Proxy.class)
@GeneratePrism(DependencyMeta.class)
@GeneratePrism(Bean.class)
@GeneratePrism(QualifiedMap.class)
@GeneratePrism(Generated.class)
@GeneratePrism(RequiresBean.class)
@GeneratePrism(RequiresProperty.class)
@GeneratePrism(value = RequiresBean.Container.class, name = "RequiresBeanContainerPrism")
@GeneratePrism(value = RequiresProperty.Container.class, name = "RequiresPropertyContainerPrism")
@GeneratePrism(Profile.class)
package io.avaje.inject.generator;

import io.avaje.inject.*;
import io.avaje.inject.aop.Aspect;
import io.avaje.inject.spi.DependencyMeta;
import io.avaje.inject.spi.Generated;
import io.avaje.inject.spi.Proxy;
import io.avaje.prism.GeneratePrism;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Qualifier;
import jakarta.inject.Scope;
import jakarta.inject.Singleton;
