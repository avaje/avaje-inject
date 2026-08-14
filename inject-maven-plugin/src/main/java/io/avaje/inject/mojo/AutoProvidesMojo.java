package io.avaje.inject.mojo;

import static java.util.stream.Collectors.toList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import io.avaje.inject.spi.AvajeModule;
import io.avaje.inject.spi.InjectExtension;
import io.avaje.inject.spi.InjectPlugin;
import io.avaje.inject.spi.PluginProvides;

/**
 * Plugin that generates <code>target/avaje-module-provides.txt</code> and <code>
 * target/avaje-plugin-provides.txt</code> based on the avaje-inject modules and plugins in the
 * classpath.
 *
 * <p>This allows the avaje-inject-generator annotation processor to be aware of all the components
 * and plugins provided by other modules in the classpath at compile time.
 */
@Mojo(
    name = "provides",
    defaultPhase = LifecyclePhase.PROCESS_SOURCES,
    requiresDependencyResolution = ResolutionScope.COMPILE,
    threadSafe = true)
public class AutoProvidesMojo extends AbstractMojo {

  private static final int MAX_SKIPPED = 10_000;

  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  @Override
  public void execute() throws MojoExecutionException {
    final var listUrl = compileDependencies();

    final var directory = new File(project.getBuild().getDirectory());
    if (!directory.exists()) {
      directory.mkdirs();
    }

    try (var newClassLoader = createClassLoader(listUrl);
        var pluginWriter = createFileWriter("avaje-plugins.csv");
        var moduleCSV = createFileWriter("avaje-module-dependencies.csv")) {

      final var extensions = loadExtensions(newClassLoader);
      writeProvidedPlugins(extensions, pluginWriter);
      writeModuleCSV(extensions, moduleCSV);

    } catch (final IOException e) {
      throw new MojoExecutionException("Failed to write spi classes", e);
    }
  }

  List<InjectExtension> loadExtensions(URLClassLoader classLoader) throws IOException {
    final Set<String> loadedNames = new HashSet<>();
    final List<InjectExtension> extensions = new ArrayList<>();
    final List<ServiceConfigurationError> skipped = new ArrayList<>();
    // a services entry can name a class from a dependency that is not resolvable at compile
    // scope (test-scoped, optional, or excluded); such a module is not a compile-time concern
    // for this project, so skip it rather than failing the build. The JDK iterator does not
    // retry an entry that failed, so catching the per-element error resumes with the next
    // entry. A provider class that is present but broken still fails the build.
    final var providers = ServiceLoader.load(InjectExtension.class, classLoader).stream().iterator();
    while (true) {
      try {
        if (!providers.hasNext()) {
          break;
        }
        final var provider = providers.next();
        extensions.add(provider.get());
        loadedNames.add(provider.type().getTypeName());
      } catch (final ServiceConfigurationError e) {
        if (!missingClass(e) || skipped.size() >= MAX_SKIPPED) {
          throw e;
        }
        skipped.add(e);
      }
    }
    if (!skipped.isEmpty()) {
      warnSkipped(classLoader, loadedNames, skipped);
    }
    return extensions;
  }

  /**
   * True when the entry failed because its class is missing rather than present but broken.
   */
  private static boolean missingClass(ServiceConfigurationError error) {
    for (Throwable cause = error.getCause(); cause != null; cause = cause.getCause()) {
      if (cause instanceof ClassNotFoundException || cause instanceof NoClassDefFoundError) {
        return true;
      }
    }
    // the JDK reports an unresolvable provider class without a cause
    return error.getCause() == null && String.valueOf(error.getMessage()).endsWith(" not found");
  }

  private void warnSkipped(URLClassLoader classLoader, Set<String> loadedNames, List<ServiceConfigurationError> skipped) throws IOException {
    for (final var declared : declaredExtensions(classLoader).entrySet()) {
      final String className = declared.getKey();
      if (loadedNames.contains(className)) {
        continue;
      }
      final String message = "Skipped InjectExtension " + className + " declared in " + declared.getValue()
        + " - not loadable from the compile-scope classpath";
      final var error = skipped.stream()
        .filter(e -> String.valueOf(e.getMessage()).contains(className))
        .findFirst();
      final Throwable cause = error.map(Throwable::getCause).orElse(null);
      if (cause != null && !(cause instanceof ClassNotFoundException)) {
        getLog().warn(message, error.get());
      } else {
        getLog().warn(error.map(e -> message + ": " + e).orElse(message));
      }
    }
  }

  private Map<String, List<URL>> declaredExtensions(URLClassLoader classLoader) throws IOException {
    final Map<String, List<URL>> declared = new LinkedHashMap<>();
    final var resources = classLoader.getResources("META-INF/services/" + InjectExtension.class.getName());
    while (resources.hasMoreElements()) {
      final URL resource = resources.nextElement();
      try (var reader = new BufferedReader(new InputStreamReader(resource.openStream(), StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          final int comment = line.indexOf('#');
          if (comment >= 0) {
            line = line.substring(0, comment);
          }
          line = line.trim();
          if (!line.isEmpty()) {
            declared.computeIfAbsent(line, key -> new ArrayList<>()).add(resource);
          }
        }
      }
    }
    return declared;
  }

  private List<URL> compileDependencies() throws MojoExecutionException {
    final List<URL> listUrl = new ArrayList<>();
    project.setArtifactFilter(new ScopeArtifactFilter("compile"));
    for (final Artifact artifact : project.getArtifacts()) {
      try {
        listUrl.add(artifact.getFile().toURI().toURL());
      } catch (final MalformedURLException e) {
        throw new MojoExecutionException("Failed to get compile dependencies", e);
      }
    }
    return listUrl;
  }

  private URLClassLoader createClassLoader(List<URL> listUrl) {
    return new URLClassLoader(listUrl.toArray(new URL[listUrl.size()]), Thread.currentThread().getContextClassLoader());
  }

  private FileWriter createFileWriter(String string) throws IOException {
    return new FileWriter(new File(project.getBuild().getDirectory(), string));
  }

  private void writeProvidedPlugins(List<InjectExtension> extensions, FileWriter pluginWriter) throws IOException {
    final Log log = getLog();

    final List<InjectPlugin> plugins = new ArrayList<>();
    extensions.stream()
        .filter(InjectPlugin.class::isInstance)
        .map(InjectPlugin.class::cast)
        .forEach(plugins::add);

    final Map<String, List<String>> pluginEntries = new HashMap<>();
    for (final var plugin : plugins) {
      final List<String> provides = new ArrayList<>();
      final var typeName = plugin.getClass();
      log.info("Loaded Plugin: " + typeName);
      for (final var provide : plugin.provides()) {
        provides.add(provide.getTypeName());
      }
      for (final var provide : plugin.providesAspects()) {
        provides.add(wrapAspect(provide.getCanonicalName()));
      }
      pluginEntries.put(typeName.getTypeName(), provides);
      Optional.ofNullable(plugin.getClass().getAnnotation(PluginProvides.class))
        .ifPresent(p -> {
          for (final var provide : p.value()) {
            provides.add(provide.getTypeName());
          }
          Collections.addAll(provides, p.providesStrings());
          for (final var provide : p.providesAspects()) {
            provides.add(wrapAspect(provide.getCanonicalName()));
          }
          p.providesStrings();
        });
    }

    pluginWriter.write("External Plugin Type|Provides");
    for (final var providedType : pluginEntries.entrySet()) {
      pluginWriter.write("\n");
      pluginWriter.write(providedType.getKey());
      pluginWriter.write("|");
      var provides = String.join(",", providedType.getValue());
      pluginWriter.write(provides.isEmpty() ? " " : provides);
    }
  }

  private void writeModuleCSV(List<InjectExtension> extensions, FileWriter moduleWriter) throws IOException {
    final Log log = getLog();
    final List<AvajeModule> avajeModules = new ArrayList<>();
    extensions.stream()
        .filter(AvajeModule.class::isInstance)
        .map(AvajeModule.class::cast)
        .forEach(avajeModules::add);

    List<ModuleData> modules = new ArrayList<>();
    for (final var module : avajeModules) {
      final var name = module.getClass();
      log.info("Detected External Module: " + name);

      final var provides = new ArrayList<String>();
      for (final var provide : module.providesBeans()) {
        var type = provide;
        provides.add(type);
      }

      final var requires = Arrays.stream(module.requiresBeans()).collect(toList());

      Collections.addAll(requires, module.requiresPackagesFromType());
      modules.add(new ModuleData(name.getTypeName(), provides, requires));
    }

    moduleWriter.write("External Module Type|Provides|Requires");
    for (ModuleData avajeModule : modules) {
      moduleWriter.write("\n");
      moduleWriter.write(avajeModule.name());
      moduleWriter.write("|");
      var provides = String.join(",", avajeModule.provides());
      moduleWriter.write(provides.isEmpty() ? " " : provides);
      moduleWriter.write("|");
      var requires = String.join(",", avajeModule.requires());
      moduleWriter.write(requires.isEmpty() ? " " : requires);
    }
  }

  private static String wrapAspect(String aspect) {
    return "io.avaje.inject.aop.AspectProvider<" + aspect + ">";
  }
}
