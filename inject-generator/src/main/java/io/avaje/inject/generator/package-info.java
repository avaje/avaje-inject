@GeneratePrism(Assisted.class)
@GeneratePrism(AssistFactory.class)
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
@GeneratePrism(AOPFallback.class)
@GeneratePrism(Aspect.class)
@GeneratePrism(value = Aspect.Import.class, name = "AspectImportPrism")
@GeneratePrism(Primary.class)
@GeneratePrism(Secondary.class)
@GeneratePrism(Proxy.class)
@GeneratePrism(PreDestroy.class)
@GeneratePrism(DependencyMeta.class)
@GeneratePrism(Bean.class)
@GeneratePrism(QualifiedMap.class)
@GeneratePrism(Generated.class)
@GeneratePrism(RequiresBean.class)
@GeneratePrism(RequiresProperty.class)
@GeneratePrism(value = RequiresBean.Container.class, name = "RequiresBeanContainerPrism")
@GeneratePrism(value = RequiresProperty.Container.class, name = "RequiresPropertyContainerPrism")
@GeneratePrism(Profile.class)
@GeneratePrism(Observes.class)
@GeneratePrism(EventType.class)
package io.avaje.inject.generator;

import io.avaje.inject.*;
import io.avaje.inject.aop.*;
import io.avaje.inject.spi.*;
import io.avaje.inject.events.*;
import io.avaje.prism.GeneratePrism;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Qualifier;
import jakarta.inject.Scope;
import jakarta.inject.Singleton;
