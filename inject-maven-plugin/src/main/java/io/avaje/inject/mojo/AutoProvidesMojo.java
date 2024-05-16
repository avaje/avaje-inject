package io.avaje.inject.mojo;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
import io.avaje.inject.spi.InjectPlugin;
import io.avaje.inject.spi.Module;
import io.avaje.inject.spi.Plugin;

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
    requiresDependencyResolution = ResolutionScope.COMPILE)
public class AutoProvidesMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  private final List<AvajeModuleData> modules = new ArrayList<>();

  @Override
  public void execute() throws MojoExecutionException {
    final var listUrl = compileDependencies();

    final var directory = new File(project.getBuild().getDirectory());
    if (!directory.exists()) {
      directory.mkdirs();
    }

    try (var newClassLoader = createClassLoader(listUrl);
        var moduleWriter = createFileWriter("avaje-module-provides.txt");
        var pluginWriter = createFileWriter("avaje-plugin-provides.txt");
        var moduleCSV = createFileWriter("avaje-module-dependencies.csv")) {

      writeProvidedPlugins(newClassLoader, pluginWriter);
      writeProvidedModules(newClassLoader, moduleWriter);
      writeModuleCSV(moduleCSV);

    } catch (final IOException e) {
      throw new MojoExecutionException("Failed to write spi classes", e);
    }
  }

  private List<URL> compileDependencies() throws MojoExecutionException {
    final List<URL> listUrl = new ArrayList<>();
    project.setArtifactFilter(new ScopeArtifactFilter("compile"));
    final var deps = project.getArtifacts();

    for (final Artifact artifact : deps) {
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
    return new FileWriter(new File(project.getBuild().getDirectory(), string), true);
  }

  private void writeProvidedPlugins(URLClassLoader newClassLoader, FileWriter pluginWriter) throws IOException {
    final Set<String> providedTypes = new HashSet<>();

    final Log log = getLog();

    final List<InjectPlugin> plugins = new ArrayList<>();
    ServiceLoader.load(Plugin.class, newClassLoader).forEach(plugins::add);
    ServiceLoader.load(InjectPlugin.class, newClassLoader).forEach(plugins::add);

    for (final var plugin : plugins) {
      log.info("Loaded Plugin: " + plugin.getClass().getTypeName());
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

  private void writeProvidedModules(URLClassLoader newClassLoader, FileWriter moduleWriter) throws IOException {
    final Set<String> providedTypes = new HashSet<>();

    final Log log = getLog();
    final List<AvajeModule> avajeModules = new ArrayList<>();
    ServiceLoader.load(Module.class, newClassLoader).forEach(avajeModules::add);
    ServiceLoader.load(AvajeModule.class, newClassLoader).forEach(avajeModules::add);

    for (final var module : avajeModules) {
      final var name = module.getClass().getTypeName();
      log.info("Detected External Module: " + name);

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
        .map(AutoProvidesMojo::wrapAspect)
        .forEach(requires::add);
      modules.add(new AvajeModuleData(name, provides, requires));
    }

    for (final String providedType : providedTypes) {
      moduleWriter.write(providedType);
      moduleWriter.write("\n");
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

  private static String wrapAspect(String aspect) {
    return "io.avaje.inject.aop.AspectProvider<" + aspect + ">";
  }
}
