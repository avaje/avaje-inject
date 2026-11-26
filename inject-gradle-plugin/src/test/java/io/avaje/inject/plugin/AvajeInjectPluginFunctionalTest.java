package io.avaje.inject.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Runs the plugin in a real (throwaway) Gradle build to verify its task wiring. */
class AvajeInjectPluginFunctionalTest {

  @TempDir Path projectDir;

  @Test
  void javaPluginAppliedFirst_compileJavaWritesModuleFiles() throws IOException {
    writeBuild(
        """
        plugins {
          id 'java'
          id 'io.avaje.inject'
        }
        """);
    writeMainClass();

    run("compileJava");

    assertModuleFilesWritten();
  }

  @Test
  void javaPluginAppliedAfter_compileJavaWritesModuleFiles() throws IOException {
    writeBuild(
        """
        plugins {
          id 'io.avaje.inject'
          id 'java'
        }
        """);
    writeMainClass();

    run("compileJava");

    assertModuleFilesWritten();
  }

  @Test
  void discoverModules_writesModuleFilesWithoutCompiling() throws IOException {
    writeBuild(
        """
        plugins {
          id 'java'
          id 'io.avaje.inject'
        }
        """);

    final BuildResult result = run("discoverModules");

    assertModuleFilesWritten();
    assertThat(result.getOutput()).doesNotContain("compileJava");
  }

  @Test
  void withoutJavaPlugin_applyDoesNotFail() throws IOException {
    writeBuild(
        """
        plugins {
          id 'io.avaje.inject'
        }
        """);

    final BuildResult result = run("tasks", "--all");

    assertThat(result.getOutput()).contains("discoverModules");
  }

  @Test
  void mainClassFileAfterCompile_setsJarManifestMainClass() throws IOException {
    writeBuild(
        """
        plugins {
          id 'java'
          id 'io.avaje.inject'
        }
        compileJava.doFirst {
          // stands in for the annotation processor writing the file
          file("$buildDir/avaje-main-class.txt").text = 'org.example.Main'
        }
        """);
    writeMainClass();

    run("jar");

    try (var jar = new JarFile(projectDir.resolve("build/libs/sample.jar").toFile())) {
      assertThat(jar.getManifest().getMainAttributes().getValue("Main-Class")).isEqualTo("org.example.Main");
    }
  }

  /** compileJava is skipped (NO-SOURCE) without any sources, so its actions would never run. */
  private void writeMainClass() throws IOException {
    Files.createDirectories(projectDir.resolve("src/main/java/org/example"));
    Files.writeString(
        projectDir.resolve("src/main/java/org/example/Main.java"),
        "package org.example; public class Main { public static void main(String[] a) {} }");
  }

  private void assertModuleFilesWritten() {
    assertThat(projectDir.resolve("build/avaje-plugins.csv")).exists();
    assertThat(projectDir.resolve("build/avaje-module-dependencies.csv")).exists();
  }

  private void writeBuild(String script) throws IOException {
    Files.writeString(projectDir.resolve("settings.gradle"), "rootProject.name = 'sample'");
    Files.writeString(projectDir.resolve("build.gradle"), script);
  }

  private BuildResult run(String... args) {
    return GradleRunner.create()
        .withProjectDir(projectDir.toFile())
        .withPluginClasspath()
        .withArguments(args)
        .forwardOutput()
        .build();
  }
}
