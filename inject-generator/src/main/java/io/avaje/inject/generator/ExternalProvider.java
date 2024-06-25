package io.avaje.inject.generator;

import static java.util.Map.entry;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;

import java.net.URI;
import java.nio.file.Paths;
import static java.util.List.of;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.tools.StandardLocation;

import io.avaje.inject.spi.AvajeModule;
import io.avaje.inject.spi.InjectPlugin;

/**
 * The types provided by other modules in the classpath at compile time.
 *
 * <p>When we depend on these types they add to the module autoRequires() classes.
 */
final class ExternalProvider {

  private static final ClassLoader CLASS_LOADER = ExternalProvider.class.getClassLoader();
  private static final boolean injectAvailable = moduleCP();
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

  private ExternalProvider() {}

  private static boolean moduleCP() {
    try {
      Class.forName(Constants.MODULE);
      return true;
    } catch (final ClassNotFoundException e) {
      return false;
    }
  }

  static void registerModuleProvidedTypes(Set<String> providedTypes) {
    if (!injectAvailable) {
      if (!pluginExists("build/avaje-module-provides.txt")
        && !pluginExists("target/avaje-module-provides.txt")) {
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
      final var provides = new ArrayList<String>();
      APContext.logNote("Detected Module: " + name);
      for (final var provide : module.provides()) {
        providedTypes.add(provide.getTypeName());
        provides.add(provide.getTypeName());
      }
      for (final var provide : module.autoProvides()) {
        providedTypes.add(provide.getTypeName());
        provides.add(provide.getTypeName());
      }
      for (final var provide : module.autoProvidesAspects()) {
        final var aspectType = Util.wrapAspect(provide.getTypeName());
        providedTypes.add(aspectType);
        provides.add(aspectType);
      }
      final var requires = Arrays.stream(module.requires()).map(Type::getTypeName).collect(toList());

      Arrays.stream(module.autoRequires()).map(Type::getTypeName).forEach(requires::add);
      Arrays.stream(module.requiresPackages()).map(Type::getTypeName).forEach(requires::add);
      Arrays.stream(module.autoRequiresAspects()).map(Type::getTypeName).map(Util::wrapAspect).forEach(requires::add);

      ProcessingContext.addModule(new ModuleData(name, provides, requires));
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
    if (!injectAvailable) {
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
      final String resource =
        APContext.filer()
          .getResource(StandardLocation.CLASS_OUTPUT, "", relativeName)
          .toUri()
          .toString()
          .replaceFirst("/target/classes", "")
          .replaceFirst("/build/classes/java/main", "");
      return Paths.get(new URI(resource)).toFile().exists();
    } catch (final Exception e) {
      return false;
    }
  }
}
