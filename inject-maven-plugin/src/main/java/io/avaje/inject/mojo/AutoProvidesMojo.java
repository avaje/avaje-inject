package io.avaje.inject.mojo;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
 * Plugin that generates <code>target/avaje-module-dependencies.csv</code> and <code>
 * target/avaje-plugins.csv</code> based on the avaje-inject modules and plugins in the
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

  private static final String SERVICES_FILE = "META-INF/services/" + InjectExtension.class.getName();

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

  /**
   * Load the {@link InjectExtension} services declared on the given classloader, skipping any entry
   * whose class is not resolvable.
   */
  List<InjectExtension> loadExtensions(ClassLoader classLoader) throws IOException {
    return declaredExtensions(classLoader).entrySet().stream()
        .map(declared -> loadExtension(declared.getKey(), declared.getValue(), classLoader))
        .flatMap(Optional::stream)
        .collect(toList());
  }

  private Optional<InjectExtension> loadExtension(
      String className, String declaredIn, ClassLoader classLoader) {
    try {
      return Optional.of(
          Class.forName(className, false, classLoader)
              .asSubclass(InjectExtension.class)
              .getDeclaredConstructor()
              .newInstance());
    } catch (final Throwable e) {
      getLog()
          .warn(
              "Skipping InjectExtension "
                  + className
                  + " declared in "
                  + declaredIn
                  + " - not loadable from the compile-scope classpath: "
                  + e);
      return Optional.empty();
    }
  }

  /**
   * Declared service entries, in classpath order, mapped to the services file(s) declaring them.
   */
  private static Map<String, String> declaredExtensions(ClassLoader classLoader)
      throws IOException {
    final Map<String, String> declared = new LinkedHashMap<>();
    for (final URL resource : Collections.list(classLoader.getResources(SERVICES_FILE))) {
      for (final String className : readServiceEntries(resource)) {
        declared.merge(className, resource.toString(), (first, next) -> first + ", " + next);
      }
    }
    return declared;
  }

  private static List<String> readServiceEntries(URL resource) throws IOException {
    try (var reader =
        new BufferedReader(new InputStreamReader(resource.openStream(), StandardCharsets.UTF_8))) {
      return reader
          .lines()
          .map(AutoProvidesMojo::stripComment)
          .filter(not(String::isEmpty))
          .collect(toList());
    } catch (final UncheckedIOException e) {
      throw e.getCause();
    }
  }

  private static String stripComment(String line) {
    final int comment = line.indexOf('#');
    return (comment < 0 ? line : line.substring(0, comment)).trim();
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

    final List<InjectPlugin> plugins = extensions.stream()
        .filter(InjectPlugin.class::isInstance)
        .map(InjectPlugin.class::cast)
        .collect(toList());

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
    final List<AvajeModule> avajeModules = extensions.stream()
        .filter(AvajeModule.class::isInstance)
        .map(AvajeModule.class::cast)
        .collect(toList());

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

      final var softRequires = Arrays.stream(module.softRequiresBeans())
        .filter(s -> !requires.contains(s))
        .collect(toList());

      modules.add(new ModuleData(name.getTypeName(), provides, requires, softRequires));
    }

    moduleWriter.write("External Module Type|Provides|Requires|SoftRequires");
    for (ModuleData avajeModule : modules) {
      moduleWriter.write("\n");
      moduleWriter.write(avajeModule.name());
      moduleWriter.write("|");
      var provides = String.join(",", avajeModule.provides());
      moduleWriter.write(provides.isEmpty() ? " " : provides);
      moduleWriter.write("|");
      var requires = String.join(",", avajeModule.requires());
      moduleWriter.write(requires.isEmpty() ? " " : requires);
      moduleWriter.write("|");
      var softRequires = String.join(",", avajeModule.softRequires());
      moduleWriter.write(softRequires.isEmpty() ? " " : softRequires);
    }
  }

  private static String wrapAspect(String aspect) {
    return "io.avaje.inject.aop.AspectProvider<" + aspect + ">";
  }
}
