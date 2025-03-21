package io.avaje.inject.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

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
}
