package io.avaje.inject.generator;

import io.avaje.inject.spi.AvajeModule;
import io.avaje.inject.spi.InjectPlugin;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.lang.reflect.Type;
import java.util.*;

import static java.util.List.of;
import static java.util.Map.entry;
import static java.util.stream.Collectors.toList;


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
      if (!pluginExists("avaje-module-provides.txt")) {
        APContext.logNote("Unable to detect Avaje Inject in Annotation Processor ClassPath, use the Avaje Inject Maven/Gradle plugin for detecting Inject Modules from dependencies");
      }
      return;
    }

    List<AvajeModule> modules = LoadServices.loadModules(CLASS_LOADER);
    if (modules.isEmpty()) {
      APContext.logNote("No external modules detected");
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

      ProcessingContext.addModule(new ModuleData(name, new ArrayList<>(provides), requires));
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
}
