package io.avaje.inject.mojo;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;

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

      writeProvidedPlugins(newClassLoader, pluginWriter);
      writeModuleCSV(newClassLoader, moduleCSV);

    } catch (final IOException e) {
      throw new MojoExecutionException("Failed to write spi classes", e);
    }
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
    return new URLClassLoader(
        listUrl.toArray(new URL[listUrl.size()]), Thread.currentThread().getContextClassLoader());
  }

  private FileWriter createFileWriter(String string) throws IOException {
    return new FileWriter(new File(project.getBuild().getDirectory(), string));
  }

  private void writeProvidedPlugins(URLClassLoader newClassLoader, FileWriter pluginWriter)
      throws IOException {
    final Log log = getLog();

    final List<InjectPlugin> plugins = new ArrayList<>();
    ServiceLoader.load(InjectExtension.class, newClassLoader).stream()
        .map(Provider::get)
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

  private void writeModuleCSV(ClassLoader newClassLoader, FileWriter moduleWriter)
      throws IOException {
    final Log log = getLog();
    final List<AvajeModule> avajeModules = new ArrayList<>();
    ServiceLoader.load(InjectExtension.class, newClassLoader).stream()
        .map(Provider::get)
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

      for (final var provide : module.autoProvidesBeans()) {
        var type = provide;
        provides.add(type);
      }

      for (final var provide : module.autoProvidesAspectBeans()) {
        var type = wrapAspect(provide);
        provides.add(type);
      }

      final var requires = Arrays.stream(module.requiresBeans()).collect(toList());

      Collections.addAll(requires, module.autoRequiresBeans());
      Collections.addAll(requires, module.requiresPackagesFromType());
      Arrays.stream(module.autoRequiresAspectBeans())
          .map(AutoProvidesMojo::wrapAspect)
          .forEach(requires::add);
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
