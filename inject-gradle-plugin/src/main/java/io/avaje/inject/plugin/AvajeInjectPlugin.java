package io.avaje.inject.plugin;

import org.gradle.api.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Plugin that discovers external avaje inject modules and plugins.
 */
public class AvajeInjectPlugin implements Plugin<Project> {

  @Override
  public void apply(Project project) {
    project.afterEvaluate(prj -> {
      // run it automatically after clean
      Task cleanTask = prj.getTasks().getByName("clean");
      cleanTask.doLast( it -> writeProvides(project));
    });
    // register a task to run it manually
    project
      .task("discoverModules")
      .doLast(task -> writeProvides(project));
  }

  private void writeProvides(Project project) {
    final var outputDir = project.getBuildDir();
    if (!outputDir.exists()) {
      if (!outputDir.mkdirs()) {
        System.err.println("Unsuccessful creating build directory");
      }
    }
    try {
      final var classLoader = classLoader(project);
      try (
        var moduleWriter = createFileWriter(outputDir.getPath(), "avaje-module-provides.txt");
        var pluginWriter = createFileWriter(outputDir.getPath(), "avaje-plugin-provides.txt")) {

        writeProvidedPlugins(classLoader, pluginWriter);
        writeProvidedModules(classLoader, moduleWriter);
      }
    } catch (IOException e) {
      throw new GradleException("Failed to write avaje-module-provides", e);
    }
  }

  private FileWriter createFileWriter(String dir, String file) throws IOException {
    return new FileWriter(new File(dir, file));
  }

  private void writeProvidedPlugins(ClassLoader cl, FileWriter pluginWriter) throws IOException {
	    final Set<String> providedTypes = new HashSet<>();

	    for (final var plugin : ServiceLoader.load(io.avaje.inject.spi.Plugin.class, cl)) {
	      for (final Class<?> provide : plugin.provides()) {
	        providedTypes.add(provide.getCanonicalName());
	      }
	      for (final Class<?> provide : plugin.providesAspects()) {
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
    for (final io.avaje.inject.spi.Module module : ServiceLoader.load(io.avaje.inject.spi.Module.class, classLoader)) {
      for (final Class<?> provide : module.provides()) {
        providedTypes.add(provide.getCanonicalName());
      }
      for (final Class<?> provide : module.autoProvides()) {
        providedTypes.add(provide.getCanonicalName());
      }
      for (final Class<?> provide : module.autoProvidesAspects()) {
        providedTypes.add(wrapAspect(provide.getCanonicalName()));
      }
    }

    for (final String providedType : providedTypes) {
      moduleWriter.write(providedType);
      moduleWriter.write("\n");
    }
  }

  private static String wrapAspect(String aspect) {
    return "io.avaje.inject.aop.AspectProvider<" + aspect + ">";
  }

  private ClassLoader classLoader(Project project) {
    final URL[] urls = createClassPath(project);
    return  new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
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
}
