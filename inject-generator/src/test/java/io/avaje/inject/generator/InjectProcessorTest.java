package io.avaje.inject.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class InjectProcessorTest {

  @AfterEach
  void deleteGeneratedFiles() {
    try {
      Stream.of(
              Files.walk(Paths.get("io").toAbsolutePath()),
              Files.walk(Paths.get("lang").toAbsolutePath()),
              Files.walk(Paths.get("util").toAbsolutePath()))
          .flatMap(Function.identity())
          .sorted(Comparator.reverseOrder())
          .map(Path::toFile)
          .forEach(File::delete);
      Paths.get("io.avaje.inject.spi.InjectExtension").toAbsolutePath().toFile().delete();
    } catch (final Exception e) {
    }
  }

  //@Disabled
  @Test
  void testGeneration() throws Exception {

    System.setProperty("append.debug", "true");

    final String source =
        Paths.get("src/test/java/io/avaje/inject/generator/models/valid")
            .toAbsolutePath()
            .toString();

    final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    final StandardJavaFileManager manager = compiler.getStandardFileManager(null, null, null);

    manager.setLocation(StandardLocation.SOURCE_PATH, List.of(new File(source)));

    final Set<Kind> fileKinds = Collections.singleton(Kind.SOURCE);

    final Iterable<JavaFileObject> files =
        manager.list(StandardLocation.SOURCE_PATH, "", fileKinds, true);

    final CompilationTask task =
        compiler.getTask(
            new PrintWriter(System.out),
            null,
            null,
            List.of("--release=" + Integer.getInteger("java.specification.version")),
            null,
            files);
    task.setProcessors(List.of(new InjectProcessor()));

    assertThat(task.call()).isTrue();
  }

  @Test
  void retainsInjectorsDuringPartialCompilation(@TempDir Path tempDir) throws Exception {
    final Path classOutput = Files.createDirectories(tempDir.resolve("classes"));
    final Path sourceOutput = Files.createDirectories(tempDir.resolve("generated"));
    final Path depSource =
        writeSource(
            tempDir,
            "partial/Dep.java",
            "package partial;\n"
                + "import jakarta.inject.Singleton;\n"
                + "@Singleton\n"
                + "public class Dep {}\n");
    final Path firstSource =
        writeSource(
            tempDir,
            "partial/First.java",
            "package partial;\n"
                + "import io.avaje.inject.InjectorTarget;\n"
                + "import jakarta.inject.Inject;\n"
                + "@InjectorTarget\n"
                + "public class First {\n"
                + "  @Inject Dep value;\n"
                + "}\n");
    final Path secondSource =
        writeSource(
            tempDir,
            "partial/Second.java",
            "package partial;\n"
                + "import io.avaje.inject.InjectorTarget;\n"
                + "import jakarta.inject.Inject;\n"
                + "@InjectorTarget\n"
                + "public class Second {\n"
                + "  @Inject Dep value;\n"
                + "}\n");

    assertThat(compile(List.of(depSource, firstSource, secondSource), classOutput, sourceOutput, null))
        .isTrue();

    final Path moduleSource = sourceOutput.resolve("partial/PartialModule.java");
    assertThat(Files.readString(moduleSource))
        .contains("partial.First")
        .contains("partial.Second")
        .contains("dependsOn = {\"partial.Dep\"}");

    Files.deleteIfExists(moduleSource);
    Files.deleteIfExists(sourceOutput.resolve("partial/First$InstanceInjector.java"));

    assertThat(compile(List.of(firstSource), classOutput, sourceOutput, classOutput)).isTrue();
    assertThat(Files.readString(moduleSource))
        .contains("partial.First")
        .contains("partial.Second")
        .contains("dependsOn = {\"partial.Dep\"}")
        .contains("partial.Second$InstanceInjector.register(builder)");

    // Partial compile of a non-injector bean still retains all injectors and their dependsOn metadata
    Files.deleteIfExists(moduleSource);
    final Path otherBean =
        writeSource(
            tempDir,
            "partial/OtherBean.java",
            "package partial;\n"
                + "import jakarta.inject.Singleton;\n"
                + "@Singleton\n"
                + "public class OtherBean {}\n");
    assertThat(compile(List.of(otherBean), classOutput, sourceOutput, classOutput)).isTrue();
    assertThat(Files.readString(moduleSource))
        .contains("partial.OtherBean")
        .contains("partial.First$InstanceInjector.register(builder)")
        .contains("partial.Second$InstanceInjector.register(builder)")
        .contains("dependsOn = {\"partial.Dep\"}");
  }

  @Test
  void reportsMissingInjectorDependency(@TempDir Path tempDir) throws Exception {
    final Path classOutput = Files.createDirectories(tempDir.resolve("classes"));
    final Path sourceOutput = Files.createDirectories(tempDir.resolve("generated"));
    final Path missingSource =
        writeSource(
            tempDir,
            "missing/NotABean.java",
            "package missing;\n"
                + "public class NotABean {}\n");
    final Path targetSource =
        writeSource(
            tempDir,
            "missing/Target.java",
            "package missing;\n"
                + "import io.avaje.inject.InjectorTarget;\n"
                + "import jakarta.inject.Inject;\n"
                + "@InjectorTarget\n"
                + "public class Target {\n"
                + "  @Inject NotABean dep;\n"
                + "}\n");
    final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

    assertThat(
            compile(
                List.of(missingSource, targetSource), classOutput, sourceOutput, null, diagnostics))
        .isFalse();
    assertThat(
            diagnostics.getDiagnostics().stream()
                .map(diagnostic -> diagnostic.getMessage(null))
                .collect(Collectors.joining("\n")))
        .contains("No dependency provided for missing.NotABean on missing.Target");
  }

  @Test
  void reportsInaccessibleInheritedInjectionMembers(@TempDir Path tempDir) throws Exception {
    final Path classOutput = Files.createDirectories(tempDir.resolve("classes"));
    final Path sourceOutput = Files.createDirectories(tempDir.resolve("generated"));
    final Path baseSource =
        writeSource(
            tempDir,
            "parent/Base.java",
            "package parent;\n"
                + "import jakarta.inject.Inject;\n"
                + "public class Base {\n"
                + "  @Inject String inherited;\n"
                + "}\n");
    final Path targetSource =
        writeSource(
            tempDir,
            "child/Target.java",
            "package child;\n"
                + "import io.avaje.inject.InjectorTarget;\n"
                + "import jakarta.inject.Inject;\n"
                + "@InjectorTarget\n"
                + "public class Target extends parent.Base {\n"
                + "  @Inject String own;\n"
                + "}\n");
    final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

    assertThat(compile(List.of(baseSource, targetSource), classOutput, sourceOutput, null, diagnostics))
        .isFalse();
    assertThat(
            diagnostics.getDiagnostics().stream()
                .map(diagnostic -> diagnostic.getMessage(null))
                .collect(Collectors.joining("\n")))
        .contains(
            "@InjectorTarget cannot access injected member parent.Base.inherited from generated package child");
  }

  private Path writeSource(Path tempDir, String relativePath, String source) throws Exception {
    final Path path = tempDir.resolve(relativePath);
    Files.createDirectories(path.getParent());
    Files.writeString(path, source);
    return path;
  }

  private boolean compile(
      List<Path> sources, Path classOutput, Path sourceOutput, Path previousOutput)
      throws Exception {
    return compile(sources, classOutput, sourceOutput, previousOutput, null);
  }

  private boolean compile(
      List<Path> sources,
      Path classOutput,
      Path sourceOutput,
      Path previousOutput,
      DiagnosticCollector<JavaFileObject> diagnostics)
      throws Exception {
    final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    final StandardJavaFileManager manager = compiler.getStandardFileManager(null, null, null);
    try {
      manager.setLocation(StandardLocation.CLASS_OUTPUT, List.of(classOutput.toFile()));
      manager.setLocation(StandardLocation.SOURCE_OUTPUT, List.of(sourceOutput.toFile()));

      final List<File> classPath = new ArrayList<>();
      for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
        classPath.add(new File(entry));
      }
      if (previousOutput != null) {
        classPath.add(previousOutput.toFile());
      }
      manager.setLocation(StandardLocation.CLASS_PATH, classPath);

      final CompilationTask task =
          compiler.getTask(
              new PrintWriter(System.out),
              manager,
              diagnostics,
              List.of("--release=11"),
              null,
              manager.getJavaFileObjectsFromPaths(sources));
      task.setProcessors(List.of(new InjectProcessor()));
      return task.call();
    } finally {
      manager.close();
    }
  }
}
