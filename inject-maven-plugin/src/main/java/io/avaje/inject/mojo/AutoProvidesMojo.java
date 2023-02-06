package io.avaje.inject.mojo;

import io.avaje.inject.spi.Module;
import io.avaje.inject.spi.Plugin;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

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

  @Override
  public void execute() throws MojoExecutionException {
    final var listUrl = compileDependencies();

    final var directory = new File(project.getBuild().getDirectory());
    if (!directory.exists()) {
      directory.mkdirs();
    }

    try (var newClassLoader = createClassLoader(listUrl);
        var moduleWriter = createFileWriter("avaje-module-provides.txt");
        var pluginWriter = createFileWriter("avaje-plugin-provides.txt")) {

      writeProvidedPlugins(newClassLoader, pluginWriter);
      writeProvidedModules(newClassLoader, moduleWriter);

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
    return new URLClassLoader(
        listUrl.toArray(new URL[listUrl.size()]), Thread.currentThread().getContextClassLoader());
  }

  private FileWriter createFileWriter(String string) throws IOException {
    return new FileWriter(new File(project.getBuild().getDirectory(), string));
  }

  private void writeProvidedPlugins(URLClassLoader newClassLoader, FileWriter pluginWriter)
      throws IOException {
    for (final var plugin : ServiceLoader.load(Plugin.class, newClassLoader)) {
      for (final Class<?> providedType : plugin.provides()) {
        pluginWriter.write(providedType.getCanonicalName());
        pluginWriter.write("\n");
      }
    }
  }

  private void writeProvidedModules(URLClassLoader newClassLoader, FileWriter moduleWriter)
      throws IOException {
    final Set<String> providedTypes = new HashSet<>();

    for (final Module module : ServiceLoader.load(Module.class, newClassLoader)) {
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
}
