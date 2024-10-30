package io.avaje.inject.generator;

import static java.util.List.of;
import static java.util.Map.entry;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

import io.avaje.inject.spi.AvajeModule;
import io.avaje.inject.spi.InjectPlugin;


/**
 * The types provided by other modules in the classpath at compile time.
 *
 * <p>When we depend on these types they add to the module autoRequires() classes.
 */
final class ExternalProvider {

  private static final ClassLoader CLASS_LOADER = ExternalProvider.class.getClassLoader();
  private static final boolean INJECT_AVAILABLE = moduleCP();
  private static final Map<String, List<String>> avajePlugins = Map.ofEntries(
    entry("io.avaje.inject.events.spi.ObserverManagerPlugin", of("io.avaje.inject.events.ObserverManager")),
    entry("io.avaje.jsonb.inject.DefaultJsonbProvider", of("io.avaje.jsonb.Jsonb")),
    entry("io.avaje.http.inject.DefaultResolverProvider", of("io.avaje.http.api.context.RequestContextResolver")),
    entry("io.avaje.htmx.nima.jstache.DefaultTemplateProvider",
      of(
        "io.avaje.htmx.nima.TemplateContentCache",
        "io.avaje.htmx.nima.TemplateRender")),
    entry("io.avaje.nima.provider.DefaultConfigProvider",
      of(
        "io.helidon.webserver.WebServerConfig.Builder",
        "io.helidon.webserver.http.HttpRouting.Builder")),
    entry("io.avaje.validation.inject.spi.DefaultValidatorProvider",
      of(
        "io.avaje.validation.Validator",
        "io.avaje.inject.aop.AspectProvider<io.avaje.validation.ValidMethod>")),
    entry("io.avaje.validation.http.HttpValidatorProvider", of("io.avaje.http.api.Validator")));
  private static final List<MetaData> externalMeta = new ArrayList<>();

  private ExternalProvider() {
  }

  private static boolean moduleCP() {
    try {
      Class.forName(Constants.MODULE);
      return true;
    } catch (final ClassNotFoundException e) {
      return false;
    }
  }

  static void registerModuleProvidedTypes(Set<String> providedTypes) {

    if (!INJECT_AVAILABLE) {
      return;
    }

    List<AvajeModule> modules = LoadServices.loadModules(CLASS_LOADER);
    if (modules.isEmpty()) {
      return;
    }
    for (final var module : modules) {
      final var name = module.getClass().getTypeName();
      APContext.logNote("Detected Module: " + name);
      final var provides = new TreeSet<String>();
      for (final var provide : module.provides()) {
        provides.add(provide.getTypeName());
      }
      for (final var provide : module.autoProvides()) {
        provides.add(provide.getTypeName());
      }
      for (final var provide : module.autoProvidesAspects()) {
        final var aspectType = Util.wrapAspect(provide.getTypeName());
        provides.add(aspectType);
      }
      registerExternalMetaData(name);
      readMetaDataProvides(provides);
      providedTypes.addAll(provides);
      final var requires = Arrays.stream(module.requires()).map(Type::getTypeName).collect(toList());

      Arrays.stream(module.autoRequires()).map(Type::getTypeName).forEach(requires::add);
      Arrays.stream(module.requiresPackages()).map(Type::getTypeName).forEach(requires::add);
      Arrays.stream(module.autoRequiresAspects()).map(Type::getTypeName).map(Util::wrapAspect).forEach(requires::add);

      ProcessingContext.addModule(new ModuleData(name, List.copyOf(provides), requires));
    }
  }


  /**
   * Register types provided by the plugin so no compiler error when we have a dependency on these
   * types and the only thing providing them is the plugin.
   */
  static void registerPluginProvidedTypes(ScopeInfo defaultScope) {
    avajePlugins.forEach((k, v) -> {
      if (APContext.typeElement(k) != null) {
        APContext.logNote("Loaded Plugin: " + k);
        v.forEach(defaultScope::pluginProvided);
      }
    });
    defaultScope.pluginProvided("io.avaje.inject.event.ObserverManager");
    if (!INJECT_AVAILABLE) {
      if (!pluginExists("avaje-module-dependencies.csv")) {
        APContext.logNote(
            "Unable to detect Avaje Inject in Annotation Processor ClassPath, use the Avaje Inject Maven/Gradle plugin for detecting Inject Plugins from dependencies");
      }
      return;
    }

    List<InjectPlugin> plugins = LoadServices.loadPlugins(CLASS_LOADER);
    for (final var plugin : plugins) {
      var name = plugin.getClass().getTypeName();
      if (avajePlugins.containsKey(name)) {
        continue;
      }
      APContext.logNote("Loaded Plugin: " + plugin.getClass().getTypeName());
      for (final var provide : plugin.provides()) {
        defaultScope.pluginProvided(provide.getTypeName());
      }
      for (final var provide : plugin.providesAspects()) {
        defaultScope.pluginProvided(Util.wrapAspect(provide.getTypeName()));
      }
    }
  }

  private static boolean pluginExists(String relativeName) {
    try {
      return APContext.getBuildResource(relativeName).toFile().exists();
    } catch (final Exception e) {
      return false;
    }
  }

  static void registerExternalMetaData(String name) {
    Optional.ofNullable(APContext.typeElement(name))
      .map(TypeElement::getEnclosedElements)
      .map(ElementFilter::methodsIn)
      .stream()
      .flatMap(List::stream)
      .map(DependencyMetaPrism::getInstanceOn)
      .filter(Objects::nonNull)
      .map(MetaData::new)
      .forEach(externalMeta::add);
  }

  static void readMetaDataProvides(Collection<String> providedTypes) {
    externalMeta.forEach(meta -> {
      providedTypes.add(meta.key());
      providedTypes.add(meta.type());
      providedTypes.addAll(Util.addQualifierSuffix(meta.provides(), meta.name()));
      providedTypes.addAll(Util.addQualifierSuffix(meta.autoProvides(), meta.name()));
    });
  }

  public static void scanTheWorld(Collection<String> providedTypes) {

    if (!externalMeta.isEmpty()) {

      return;
    }

    var types = APContext.types();
    var spi = APContext.typeElement("io.avaje.inject.spi.AvajeModule").asType();
    APContext.elements().getAllModuleElements().stream()
        .filter(m -> !m.getQualifiedName().toString().startsWith("java"))
        .filter(m -> !m.getQualifiedName().toString().startsWith("jdk"))
        // for whatever reason, compilation breaks if we don't filter out the current module
        .filter(m -> m != APContext.getProjectModuleElement())
        .flatMap(m -> m.getEnclosedElements().stream())
        .flatMap(p -> p.getEnclosedElements().stream())
        .map(TypeElement.class::cast)
        .filter(t -> t.getKind() == ElementKind.CLASS)
        .filter(t -> t.getModifiers().contains(Modifier.PUBLIC))
        .filter(t -> t.getInterfaces().stream().anyMatch(i -> types.isAssignable(i, spi)))
        .forEach(
            t -> {
              final var provides = new HashSet<String>();
              final var requires = new HashSet<String>();
              Optional.of(t)
                  .map(TypeElement::getEnclosedElements)
                  .map(ElementFilter::methodsIn)
                  .stream()
                  .flatMap(List::stream)
                  .map(DependencyMetaPrism::getInstanceOn)
                  .filter(Objects::nonNull)
                  .map(MetaData::new)
                  .forEach(
                      m -> {
                        externalMeta.add(m);
                        provides.addAll(m.autoProvides());
                        provides.addAll(m.provides());
                        m.dependsOn().stream()
                            .filter(d -> !d.isSoftDependency())
                            .map(Dependency::name)
                            .forEach(requires::add);

                        providedTypes.add(m.key());
                        providedTypes.add(m.type());
                        providedTypes.addAll(Util.addQualifierSuffix(m.provides(), m.name()));
                        providedTypes.addAll(Util.addQualifierSuffix(m.autoProvides(), m.name()));
                      });

              final var name = t.getQualifiedName().toString();
              APContext.logNote("Detected Module: " + name);
              ProcessingContext.addModule(
                  new ModuleData(name, List.copyOf(provides), List.copyOf(requires)));
            });
    if (externalMeta.isEmpty()) {
      APContext.logNote("No external modules detected");
    }
  }
}
