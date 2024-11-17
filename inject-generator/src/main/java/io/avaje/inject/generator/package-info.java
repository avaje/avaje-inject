@GeneratePrism(AOPFallback.class)
@GeneratePrism(Aspect.class)
@GeneratePrism(value = Aspect.Import.class, name = "AspectImportPrism")
@GeneratePrism(Assisted.class)
@GeneratePrism(AssistFactory.class)
@GeneratePrism(AsyncBean.class)
@GeneratePrism(Bean.class)
@GeneratePrism(Component.class)
@GeneratePrism(Component.Import.class)
@GeneratePrism(DependencyMeta.class)
@GeneratePrism(External.class)
@GeneratePrism(Factory.class)
@GeneratePrism(Generated.class)
@GeneratePrism(Inject.class)
@GeneratePrism(InjectModule.class)
@GeneratePrism(BeanTypes.class)
@GeneratePrism(Lazy.class)
@GeneratePrism(Named.class)
@GeneratePrism(PluginProvides.class)
@GeneratePrism(PreDestroy.class)
@GeneratePrism(Primary.class)
@GeneratePrism(Profile.class)
@GeneratePrism(Prototype.class)
@GeneratePrism(Proxy.class)
@GeneratePrism(QualifiedMap.class)
@GeneratePrism(Qualifier.class)
@GeneratePrism(RequiresBean.class)
@GeneratePrism(RequiresProperty.class)
@GeneratePrism(value = RequiresBean.Container.class, name = "RequiresBeanContainerPrism")
@GeneratePrism(value = RequiresProperty.Container.class, name = "RequiresPropertyContainerPrism")
@GeneratePrism(Singleton.class)
@GeneratePrism(Scope.class)
@GeneratePrism(Secondary.class)
@GeneratePrism(io.avaje.spi.ServiceProvider.class)
package io.avaje.inject.generator;

import io.avaje.inject.*;
import io.avaje.inject.aop.Aspect;
import io.avaje.inject.aop.AOPFallback;
import io.avaje.inject.spi.*;
import io.avaje.prism.GeneratePrism;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Qualifier;
import jakarta.inject.Scope;
import jakarta.inject.Singleton;
