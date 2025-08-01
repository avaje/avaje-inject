package io.avaje.inject.plugin;

import static java.util.stream.Collectors.toList;

import io.avaje.inject.spi.AvajeModule;
import io.avaje.inject.spi.InjectPlugin;
import io.avaje.inject.spi.InjectExtension;

import io.avaje.inject.spi.PluginProvides;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.GradleException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.ServiceLoader.Provider;

/**
 * Plugin that discovers external avaje inject modules and plugins.
 */
public class AvajeInjectPlugin implements org.gradle.api.Plugin<Project> {

  private final List<ModuleData> modules = new ArrayList<>();

  @Override
  public void apply(Project project) {
    project.afterEvaluate(
        prj -> {
          // run it automatically before build
          Task buildTask = prj.getTasks().getByName("compileJava");
          buildTask.doFirst(it -> writeProvides(project));
        });
    // register a task to run it manually
    project.task("discoverModules").doLast(task -> writeProvides(project));
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
      writeProvidedPlugins(classLoader, pluginWriter);
      writeModuleCSV(classLoader, moduleCSV);
    } catch (IOException e) {
      throw new GradleException("Failed to write avaje-module-provides", e);
    }
  }

  private FileWriter createFileWriter(String dir, String file) throws IOException {
    return new FileWriter(new File(dir, file));
  }

  private void writeProvidedPlugins(ClassLoader classLoader, FileWriter pluginWriter) throws IOException {
    final List<InjectPlugin> plugins = new ArrayList<>();
    ServiceLoader.load(InjectExtension.class, classLoader).stream()
        .map(Provider::get)
        .filter(InjectPlugin.class::isInstance)
        .map(InjectPlugin.class::cast)
        .forEach(plugins::add);

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

  private void writeModuleCSV(ClassLoader classLoader, FileWriter moduleWriter) throws IOException {

    final List<AvajeModule> avajeModules = new ArrayList<>();
    ServiceLoader.load(InjectExtension.class, classLoader).stream()
        .map(Provider::get)
        .filter(AvajeModule.class::isInstance)
        .map(AvajeModule.class::cast)
        .forEach(avajeModules::add);

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
