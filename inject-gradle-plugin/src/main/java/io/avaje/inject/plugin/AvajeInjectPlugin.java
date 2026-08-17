package io.avaje.inject.plugin;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;

import io.avaje.inject.spi.AvajeModule;
import io.avaje.inject.spi.InjectPlugin;
import io.avaje.inject.spi.InjectExtension;

import io.avaje.inject.spi.PluginProvides;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.bundling.Jar;
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
import java.nio.file.Files;
import java.util.*;

/**
 * Plugin that discovers external avaje inject modules and plugins.
 */
public class AvajeInjectPlugin implements org.gradle.api.Plugin<Project> {

  private static final String SERVICES_FILE = "META-INF/services/" + InjectExtension.class.getName();

  private final List<ModuleData> modules = new ArrayList<>();

  @Override
  public void apply(Project project) {
    project.afterEvaluate(
        prj -> {
          // run it automatically before build
          Task buildTask = prj.getTasks().getByName("compileJava");
          buildTask.doFirst(it -> writeProvides(project));
          buildTask.doLast(it -> configureMainClass(project));
        });
    // register a task to run it manually
    project.task("discoverModules").doLast(task -> writeProvides(project));
  }

  private void configureMainClass(Project project) {
    var mainClassFile = new File(project.getBuildDir(), "avaje-main-class.txt");
    if (!mainClassFile.exists()) {
      return;
    }
    try {
      var mainClass = Files.readString(mainClassFile.toPath()).trim();
      if (mainClass.isEmpty()) {
        return;
      }
      project.getTasks().withType(Jar.class).configureEach(jar ->
        jar.getManifest().getAttributes().put("Main-Class", mainClass));
      System.out.println("Configured JAR Main-Class: " + mainClass);
    } catch (IOException e) {
      System.err.println("Unable to read avaje-main-class.txt: " + e.getMessage());
    }
  }

  private void writeProvides(Project project) {
    final var outputDir = project.getBuildDir();
    if (!outputDir.exists()) {
      if (!outputDir.mkdirs()) {
        System.err.println("Unsuccessful creating build directory");
      }
    }

    try (var classLoader = classLoader(project);
        var pluginWriter = createFileWriter(outputDir.getPath(), "avaje-plugins.csv");
        var moduleCSV = createFileWriter(outputDir.getPath(), "avaje-module-dependencies.csv")) {
      final var extensions = loadExtensions(classLoader);
      writeProvidedPlugins(extensions, pluginWriter);
      writeModuleCSV(extensions, moduleCSV);
    } catch (IOException e) {
      throw new GradleException("Failed to write avaje-module-provides", e);
    }
  }

  /**
   * Load the {@link InjectExtension} services declared on the given classloader, skipping any entry
   * whose class is not resolvable.
   */
  static List<InjectExtension> loadExtensions(ClassLoader classLoader) throws IOException {
    return declaredExtensions(classLoader).entrySet().stream()
        .map(declared -> loadExtension(declared.getKey(), declared.getValue(), classLoader))
        .flatMap(Optional::stream)
        .collect(toList());
  }

  private static Optional<InjectExtension> loadExtension(String className, String declaredIn, ClassLoader classLoader) {
    try {
      return Optional.of(Class.forName(className, false, classLoader)
          .asSubclass(InjectExtension.class)
          .getDeclaredConstructor()
          .newInstance());
    } catch (Throwable e) {
      System.err.println("Skipping InjectExtension " + className + " declared in " + declaredIn
          + " - not loadable from the compile classpath: " + e);
      return Optional.empty();
    }
  }

  /** Declared service entries, in classpath order, mapped to the services file(s) declaring them. */
  private static Map<String, String> declaredExtensions(ClassLoader classLoader) throws IOException {
    final Map<String, String> declared = new LinkedHashMap<>();
    for (final URL resource : Collections.list(classLoader.getResources(SERVICES_FILE))) {
      for (final String className : readServiceEntries(resource)) {
        declared.merge(className, resource.toString(), (first, next) -> first + ", " + next);
      }
    }
    return declared;
  }

  private static List<String> readServiceEntries(URL resource) throws IOException {
    try (var reader = new BufferedReader(new InputStreamReader(resource.openStream(), StandardCharsets.UTF_8))) {
      return reader.lines()
          .map(AvajeInjectPlugin::stripComment)
          .filter(not(String::isEmpty))
          .collect(toList());
    } catch (UncheckedIOException e) {
      throw e.getCause();
    }
  }

  private static String stripComment(String line) {
    final int comment = line.indexOf('#');
    return (comment < 0 ? line : line.substring(0, comment)).trim();
  }

  private FileWriter createFileWriter(String dir, String file) throws IOException {
    return new FileWriter(new File(dir, file));
  }

  private void writeProvidedPlugins(List<InjectExtension> extensions, FileWriter pluginWriter) throws IOException {
    final List<InjectPlugin> plugins = extensions.stream()
        .filter(InjectPlugin.class::isInstance)
        .map(InjectPlugin.class::cast)
        .collect(toList());

    final Map<String, List<String>> pluginEntries = new HashMap<>();
    for (final var plugin : plugins) {

      final List<String> provides = new ArrayList<>();
      final var typeName = plugin.getClass().getTypeName();
      System.out.println("Loaded Plugin: " + typeName);
      for (final var provide : plugin.provides()) {
        provides.add(provide.getTypeName());
      }
      for (final var provide : plugin.providesAspects()) {
        provides.add(wrapAspect(provide.getCanonicalName()));
      }
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
      pluginEntries.put(typeName, provides);
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

  private static String wrapAspect(String aspect) {
    return "io.avaje.inject.aop.AspectProvider<" + aspect + ">";
  }

  private URLClassLoader classLoader(Project project) {
    final URL[] urls = createClassPath(project);
    return new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
  }

  private static URL[] createClassPath(Project project) {
    try {
      Set<File> compileClasspath =
          project.getConfigurations().getByName("compileClasspath").resolve();
      final List<URL> urls = new ArrayList<>(compileClasspath.size());
      for (File file : compileClasspath) {
        urls.add(file.toURI().toURL());
      }
      return urls.toArray(new URL[0]);
    } catch (MalformedURLException e) {
      throw new GradleException("Error building classpath", e);
    }
  }

  private void writeModuleCSV(List<InjectExtension> extensions, FileWriter moduleWriter) throws IOException {

    final List<AvajeModule> avajeModules = extensions.stream()
        .filter(AvajeModule.class::isInstance)
        .map(AvajeModule.class::cast)
        .collect(toList());

    for (final var module : avajeModules) {
      final var name = module.getClass().getTypeName();
      System.out.println("Detected External Module: " + name);

      final var provides = new ArrayList<String>();
      for (final var provide : module.providesBeans()) {
        var type = provide;
        provides.add(type);
      }

      final var requires = Arrays.stream(module.requiresBeans()).collect(toList());
      Collections.addAll(requires, module.requiresPackagesFromType());
      modules.add(new ModuleData(name, provides, requires));
    }

    moduleWriter.write("\nExternal Module Type|Provides|Requires");
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
}
