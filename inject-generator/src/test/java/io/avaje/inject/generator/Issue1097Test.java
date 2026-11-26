package io.avaje.inject.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Regression tests for https://github.com/avaje/avaje-inject/issues/1097
 *
 * <p>The Gradle plugin writes {@code avaje-plugins.csv} into the build directory itself, but the
 * {@code @MainClass} check was looking for it in the build directory's parent (the project root).
 */
class Issue1097Test {

  @TempDir Path projectRoot;

  @Test
  void gradle_pluginCsvInBuildDir_isDetected() throws IOException {
    var buildDir = Files.createDirectory(projectRoot.resolve("build"));
    Files.createFile(buildDir.resolve("avaje-plugins.csv"));

    assertThat(InjectProcessor.hasBuildPlugin(buildDir)).isTrue();
  }

  @Test
  void maven_pomInProjectRoot_isDetected() throws IOException {
    var buildDir = Files.createDirectory(projectRoot.resolve("target"));
    Files.createFile(projectRoot.resolve("pom.xml"));

    assertThat(InjectProcessor.hasBuildPlugin(buildDir)).isTrue();
  }

  @Test
  void neitherPomNorPluginCsv_isNotDetected() throws IOException {
    var buildDir = Files.createDirectory(projectRoot.resolve("build"));

    assertThat(InjectProcessor.hasBuildPlugin(buildDir)).isFalse();
  }
}
