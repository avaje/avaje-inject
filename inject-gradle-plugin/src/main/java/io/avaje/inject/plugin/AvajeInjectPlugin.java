package io.avaje.inject.plugin;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import io.avaje.inject.spi.AvajeModule;
import io.avaje.inject.spi.InjectPlugin;
import io.avaje.inject.spi.InjectSPI;
import io.avaje.inject.spi.Module;
import org.gradle.api.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.ServiceLoader.Provider;

/**
 * Plugin that discovers external avaje inject modules and plugins.
 */
public class AvajeInjectPlugin implements Plugin<Project> {

  private final List<AvajeModuleData> modules = new ArrayList<>();

  @Override
  public void apply(Project project) {
    project.afterEvaluate(
        prj -> {
          // run it automatically after clean
          Task cleanTask = prj.getTasks().getByName("clean");
          cleanTask.doLast(it -> writeProvides(project));
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
        var moduleWriter = createFileWriter(outputDir.getPath(), "avaje-module-provides.txt");
        var pluginWriter = createFileWriter(outputDir.getPath(), "avaje-plugin-provides.txt");
        var moduleCSV = createFileWriter(outputDir.getPath(), "avaje-module-dependencies.csv")) {

        writeProvidedPlugins(classLoader, pluginWriter);
        writeProvidedModules(classLoader, moduleWriter);
        writeModuleCSV(moduleCSV);

    } catch (IOException e) {
      throw new GradleException("Failed to write avaje-module-provides", e);
    }
  }

  private FileWriter createFileWriter(String dir, String file) throws IOException {
    return new FileWriter(new File(dir, file));
  }

  private void writeProvidedPlugins(ClassLoader cl, FileWriter pluginWriter) throws IOException {
    final Set<String> providedTypes = new HashSet<>();

    List<InjectPlugin> allPlugins = new ArrayList<>();
    ServiceLoader.load(io.avaje.inject.spi.Plugin.class, cl).forEach(allPlugins::add);
    ServiceLoader.load(io.avaje.inject.spi.InjectPlugin.class, cl).forEach(allPlugins::add);

    for (final var plugin : allPlugins) {
      System.out.println("Loaded Plugin: " + plugin.getClass().getCanonicalName());
      for (final var provide : plugin.provides()) {
        providedTypes.add(provide.getTypeName());
      }
      for (final var provide : plugin.providesAspects()) {
        providedTypes.add(wrapAspect(provide.getCanonicalName()));
      }
    }

    for (final var providedType : providedTypes) {
      pluginWriter.write(providedType);
      pluginWriter.write("\n");
    }
  }

  private void writeProvidedModules(ClassLoader classLoader, FileWriter moduleWriter) throws IOException {
    final Set<String> providedTypes = new HashSet<>();

    final List<AvajeModule> avajeModules = new ArrayList<>();
    ServiceLoader.load(Module.class, classLoader).forEach(avajeModules::add);
    ServiceLoader.load(InjectSPI.class, classLoader).stream()
        .map(Provider::get)
        .filter(AvajeModule.class::isInstance)
        .map(AvajeModule.class::cast)
        .forEach(avajeModules::add);
    for (final var module : avajeModules) {

      final var name = module.getClass().getTypeName();
      System.out.println("Detected External Module: " + name);

      final var provides = new ArrayList<String>();
      for (final var provide : module.provides()) {
        var type = provide.getTypeName();
        providedTypes.add(type);
        provides.add(type);
      }
      for (final var provide : module.autoProvides()) {
        var type = provide.getTypeName();
        providedTypes.add(type);
        provides.add(type);
      }
      for (final var provide : module.autoProvidesAspects()) {
        var type = wrapAspect(provide.getTypeName());
        providedTypes.add(type);
        provides.add(type);
      }

      final var requires =
          Arrays.<Type>stream(module.requires()).map(Type::getTypeName).collect(toList());

      Arrays.<Type>stream(module.autoRequires()).map(Type::getTypeName).forEach(requires::add);
      Arrays.<Type>stream(module.requiresPackages()).map(Type::getTypeName).forEach(requires::add);
      Arrays.<Type>stream(module.autoRequiresAspects())
          .map(Type::getTypeName)
          .map(AvajeInjectPlugin::wrapAspect)
          .forEach(requires::add);
      modules.add(new AvajeModuleData(name, provides, requires));
    }

    for (final String providedType : providedTypes) {
      moduleWriter.write(providedType);
      moduleWriter.write("\n");
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
      Set<File> compileClasspath = project.getConfigurations().getByName("compileClasspath").resolve();
      final List<URL> urls = new ArrayList<>(compileClasspath.size());
      for (File file : compileClasspath) {
        urls.add(file.toURI().toURL());
      }
      return urls.toArray(new URL[0]);
    } catch (MalformedURLException e) {
      throw new GradleException("Error building classpath", e);
    }
  }

  private void writeModuleCSV(FileWriter moduleWriter) throws IOException {
    moduleWriter.write("\nExternal Module Type|Provides|Requires");
    for (AvajeModuleData avajeModule : modules) {
      moduleWriter.write("\n");
      moduleWriter.write(avajeModule.name());
      moduleWriter.write("|");
      var provides = avajeModule.provides().stream().collect(joining(","));
      moduleWriter.write(provides.isEmpty() ? " " : provides);
      moduleWriter.write("|");
      var requires = avajeModule.requires().stream().collect(joining(","));
      moduleWriter.write(requires.isEmpty() ? " " : requires);
    }
  }
}
