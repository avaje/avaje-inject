package io.avaje.inject.generator;

import static java.util.Map.entry;

import java.net.URI;
import java.nio.file.Paths;
import static java.util.List.of;

import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;

import javax.tools.StandardLocation;

import io.avaje.inject.spi.Module;
import io.avaje.inject.spi.Plugin;

/**
 * The types provided by other modules in the classpath at compile time.
 *
 * <p>When we depend on these types they add to the module autoRequires() classes.
 */
final class ExternalProvider {

  private static final boolean injectAvailable = moduleCP();
  private static final Map<String, List<String>> avajePlugins = Map.ofEntries(
    entry("io.avaje.jsonb.inject.DefaultJsonbProvider",
      of("io.avaje.jsonb.Jsonb")),
    entry("io.avaje.http.inject.DefaultResolverProvider",
      of("io.avaje.http.api.context.RequestContextResolver")),
    entry("io.avaje.nima.provider.DefaultConfigProvider",
      of("io.helidon.webserver.WebServerConfig.Builder", "io.helidon.webserver.http.HttpRouting.Builder")),
    entry("io.avaje.validation.inject.spi.DefaultValidatorProvider",
      of("io.avaje.validation.Validator", "io.avaje.inject.aop.AspectProvider<io.avaje.validation.ValidMethod>")));

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
      if (!pluginExists("build/avaje-plugin-exists.txt")
          && !pluginExists("target/avaje-plugin-exists.txt")) {
        APContext.logNote(
            "Unable to detect Avaje Inject in Annotation Processor ClassPath, use the Avaje Inject Maven/Gradle plugin for detecting Inject Modules from dependencies");
      }
      return;
    }

    final var iterator = ServiceLoader.load(Module.class, ExternalProvider.class.getClassLoader()).iterator();
    if (!iterator.hasNext()) {
      APContext.logNote("No external modules detected");
      return;
    }
    while (iterator.hasNext()) {
      try {
        final var module = iterator.next();
        System.out.println("Detected Module: " + module.getClass().getCanonicalName());
        for (final Class<?> provide : module.provides()) {
          providedTypes.add(provide.getCanonicalName());
        }
        for (final Class<?> provide : module.autoProvides()) {
          providedTypes.add(provide.getCanonicalName());
        }
        for (final Class<?> provide : module.autoProvidesAspects()) {
          providedTypes.add(Util.wrapAspect(provide.getCanonicalName()));
        }
      } catch (final ServiceConfigurationError expected) {
        // ignore expected error reading the module that we are also writing
      }
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
    if (!injectAvailable) {
      return;
    }
    for (final Plugin plugin : ServiceLoader.load(Plugin.class, Processor.class.getClassLoader())) {
      var name = plugin.getClass().getCanonicalName();
      if (avajePlugins.containsKey(name)) {
        continue;
      }
      APContext.logNote("Loaded Plugin: " + plugin.getClass().getCanonicalName());
      for (final Class<?> provide : plugin.provides()) {
        defaultScope.pluginProvided(provide.getCanonicalName());
      }
      for (final Class<?> provide : plugin.providesAspects()) {
        defaultScope.pluginProvided(Util.wrapAspect(provide.getCanonicalName()));
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
