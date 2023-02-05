package io.avaje.requires;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import io.avaje.inject.spi.Module;
import io.avaje.inject.spi.Plugin;

@Mojo(
    name = "load-spi",
    defaultPhase = LifecyclePhase.PROCESS_SOURCES,
    requiresDependencyResolution = ResolutionScope.COMPILE)
public class AutoRequiresMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  @Override
  public void execute() throws MojoExecutionException {

    final var listUrl = getCompileDependencies();

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

  private List<URL> getCompileDependencies() throws MojoExecutionException {

    final List<URL> listUrl = new ArrayList<>();
    project.setArtifactFilter(new ScopeArtifactFilter("compile"));
    final var deps = project.getArtifacts();

    for (final Artifact artifact : deps) {
      try {
        URL url;
        url = artifact.getFile().toURI().toURL();
        listUrl.add(url);
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

  static String wrapAspect(String aspect) {
    return "io.avaje.inject.aop.AspectProvider<" + aspect + ">";
  }
}
