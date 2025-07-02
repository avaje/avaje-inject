package io.avaje.inject.generator;

import static java.util.List.of;
import static java.util.Map.entry;
import static java.util.stream.Collectors.toList;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.ModuleElement.ProvidesDirective;
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
      final var provides = new TreeSet<String>();
      Collections.addAll(provides, module.providesBeans());
      for (final var provide : module.autoProvidesAspects()) {
        final var aspectType = Util.wrapAspect(provide.getTypeName());
        provides.add(aspectType);
      }
      registerExternalMetaData(name);
      readMetaDataProvides(provides);
      providedTypes.addAll(provides);
      final List<String> requires = new ArrayList<>();
      Collections.addAll(requires, module.requiresBeans());
      Collections.addAll(requires, module.requiresPackagesFromType());
      Arrays.stream(module.autoRequiresAspects())
          .map(Class::getTypeName)
          .map(Util::wrapAspect)
          .forEach(requires::add);

      ProcessingContext.addModule(new ModuleData(name, List.copyOf(provides), requires));
    }
  }

  /**
   * Register types provided by the plugin so no compiler error when we have a dependency on these
   * types and the only thing providing them is the plugin.
   */
  static void registerPluginProvidedTypes(ScopeInfo defaultScope) {
    if (!INJECT_AVAILABLE) {
      if (!pluginExists("avaje-module-dependencies.csv")) {
        APContext.logNote("Unable to detect Avaje Inject Maven/Gradle plugin, use the Avaje Inject Maven/Gradle plugin for auto detecting External Inject Plugins/Modules from dependencies");
      }
      return;
    }

    List<InjectPlugin> plugins = LoadServices.loadPlugins(CLASS_LOADER);
    for (final var plugin : plugins) {
      ProcessingContext.addExternalInjectSPI(plugin.getClass().getCanonicalName());
      var name = plugin.getClass().getTypeName();
      if (avajePlugins.containsKey(name)) {
        continue;
      }
      APContext.logNote("Loaded Plugin: %s", plugin.getClass().getTypeName());
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
    ProcessingContext.addExternalInjectSPI(name);
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

  static void scanAllInjectPlugins(ScopeInfo defaultScope) {
    Map<String, List<String>> plugins = new HashMap<>();
    final var hasPlugins = !defaultScope.pluginProvided().isEmpty();
    avajePlugins.forEach((k, v) -> {
      if (APContext.typeElement(k) != null) {
        plugins.put(k, v);
        APContext.logNote("Loaded Plugin: %s", k);
        ProcessingContext.addExternalInjectSPI(k);
        v.forEach(defaultScope::pluginProvided);
      }
    });
    defaultScope.pluginProvided("io.avaje.inject.event.ObserverManager");
    if (hasPlugins) {
      return;
    }

    injectExtensions()
      .filter(PluginProvidesPrism::isPresent)
      .distinct()
      .forEach(pluginType -> addPluginToScope(defaultScope, pluginType, plugins));

    if (defaultScope.pluginProvided().isEmpty()) {
      APContext.logNote("No external plugins detected");
    }
    writePluginProvides(plugins);
  }

  private static void writePluginProvides(Map<String, List<String>> plugins) {
    // write detected plugins to a text file for test compilation
    try (final var pluginWriter = new FileWriter(APContext.getBuildResource("avaje-plugins.csv").toFile())) {
      pluginWriter.write("External Plugin Type|Provides");
      for (final var plugin : plugins.entrySet()) {
        pluginWriter.write("\n");
        pluginWriter.write(plugin.getKey());
        pluginWriter.write("|");
        var provides = String.join(",", plugin.getValue());
        pluginWriter.write(provides.isEmpty() ? " " : provides);
      }
    } catch (IOException e) {
      APContext.logWarn("Failed to write avaje-plugins.csv due to %s", e.getMessage());
    }
  }

  private static void addPluginToScope(ScopeInfo defaultScope, TypeElement pluginType, Map<String, List<String>> plugins) {
    final var name = pluginType.getQualifiedName().toString();
    ProcessingContext.addExternalInjectSPI(name);
    var prism = PluginProvidesPrism.getInstanceOn(pluginType);
    List<String> provides = new ArrayList<>();
    for (final var provide : prism.value()) {
      defaultScope.pluginProvided(provide.toString());
      provides.add(provide.toString());
    }
    for (final var provide : prism.providesStrings()) {
      defaultScope.pluginProvided(provide);
      provides.add(provide);
    }
    for (final var provide : prism.providesAspects()) {
      final var wrapAspect = Util.wrapAspect(provide.toString());
      defaultScope.pluginProvided(wrapAspect);
      provides.add(wrapAspect);
    }
    APContext.logNote("Loaded Plugin: %s", name);
    plugins.put(name, provides);
  }

  static void scanAllAvajeModules(Collection<String> providedTypes) {
    if (!externalMeta.isEmpty()) {
      return;
    }
    final var types = APContext.types();
    final var avajeModuleType = APContext.typeElement("io.avaje.inject.spi.AvajeModule").asType();
    injectExtensions()
      .filter(t -> t.getInterfaces().stream().anyMatch(i -> types.isAssignable(i, avajeModuleType)))
      .distinct()
      .forEach(otherModule -> addOtherModuleProvides(providedTypes, otherModule));

    if (externalMeta.isEmpty()) {
      APContext.logNote("No external modules detected");
    }
    writeModuleDependencies();
  }

  private static void writeModuleDependencies() {
    // write detected modules to a csv for test compilation
    try (final var moduleWriter = new FileWriter(APContext.getBuildResource("avaje-module-dependencies.csv").toFile())) {
      moduleWriter.write("External Module Type|Provides|Requires");
      for (ModuleData avajeModule : ProcessingContext.modules()) {
        moduleWriter.write("\n");
        moduleWriter.write(avajeModule.name());
        moduleWriter.write("|");
        var provides = String.join(",", avajeModule.provides());
        moduleWriter.write(provides.isEmpty() ? " " : provides);
        moduleWriter.write("|");
        var requires = String.join(",", avajeModule.requires());
        moduleWriter.write(requires.isEmpty() ? " " : requires);
      }

    } catch (IOException e) {
      APContext.logWarn("Failed to write avaje-module-dependencies.csv due to %s", e.getMessage());
    }
  }

  private static void addOtherModuleProvides(Collection<String> providedTypes, TypeElement otherModule) {
    final var provides = new HashSet<String>();
    final var requires = new HashSet<String>();

    ElementFilter.methodsIn(otherModule.getEnclosedElements()).stream()
      .map(DependencyMetaPrism::getInstanceOn)
      .filter(Objects::nonNull)
      .map(MetaData::new)
      .forEach(m -> {
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

    final var name = otherModule.getQualifiedName().toString();
    APContext.logNote("Detected Module: %s", name);
    ProcessingContext.addModule(new ModuleData(name, List.copyOf(provides), List.copyOf(requires)));
  }

  private static Stream<TypeElement> injectExtensions() {
    final var allModules =
      APContext.elements().getAllModuleElements().stream()
        .filter(m -> !m.getQualifiedName().toString().startsWith("java"))
        .filter(m -> !m.getQualifiedName().toString().startsWith("jdk"))
        // for whatever reason, compilation breaks if we don't filter out the current module
        .filter(m -> !m.equals(APContext.getProjectModuleElement()))
        .collect(toList());

    final var types = APContext.types();
    final var extensionType = APContext.typeElement("io.avaje.inject.spi.InjectExtension").asType();

    final var checkEnclosing =
      allModules.stream()
        .flatMap(ExternalProvider::getEnclosed)
        .flatMap(ExternalProvider::getEnclosed)
        .map(TypeElement.class::cast)
        .filter(t -> t.getKind() == ElementKind.CLASS)
        .filter(t -> t.getModifiers().contains(Modifier.PUBLIC))
        .filter(t -> types.isAssignable(t.asType(), extensionType));

    final var checkDirectives =
      allModules.stream()
        .flatMap(ExternalProvider::providesDirectives)
        .filter(ExternalProvider::isInjectExtension)
        .flatMap(p -> p.getImplementations().stream());

    return Stream.concat(checkEnclosing, checkDirectives);
  }

  // Automatic modules throw an NPE for getDirectives on JDT
  private static Stream<ProvidesDirective> providesDirectives(ModuleElement m) {
    try {
      return ElementFilter.providesIn(m.getDirectives()).stream();
    } catch (NullPointerException npe) {
      return Stream.of();
    }
  }

  // when a project's module-info is misconfigured a certain way, getEnclosedElements throws an error
  private static Stream<? extends Element> getEnclosed(Element e) {
    try {
      return e.getEnclosedElements().stream();
    } catch (Exception ex) {
      return Stream.of();
    }
  }

  private static boolean isInjectExtension(ModuleElement.ProvidesDirective p) {
    return "io.avaje.inject.spi.InjectExtension".equals(p.getService().getQualifiedName().toString());
  }
}
