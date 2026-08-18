package io.avaje.inject.mojo;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.avaje.inject.spi.InjectExtension;

class AutoProvidesMojoTest {

  public static class TestExtension implements InjectExtension {}

  public static class OtherTestExtension implements InjectExtension {}

  public static class NotAnExtension {}

  public static class BrokenExtension implements InjectExtension {
    public BrokenExtension() {
      throw new IllegalStateException("broken provider");
    }
  }

  @Test
  void loadExtensions_resumesAfterUnresolvableEntry(@TempDir Path dir) throws Exception {
    try (var classLoader = classLoaderWithServices(dir, getClass().getClassLoader(),
        "# comment",
        TestExtension.class.getName(),
        "does.not.Exist",
        OtherTestExtension.class.getName() + " # trailing comment")) {

      final var extensions = new AutoProvidesMojo().loadExtensions(classLoader);

      assertThat(extensions).hasExactlyElementsOfTypes(TestExtension.class, OtherTestExtension.class);
    }
  }

  @Test
  void loadExtensions_warnsNamingTheDeclaringServicesFile(@TempDir Path dir) throws Exception {
    final var mojo = new AutoProvidesMojo();
    final var warnings = capturingLog(mojo);

    try (var classLoader = classLoaderWithServices(dir, getClass().getClassLoader(), "does.not.Exist")) {
      mojo.loadExtensions(classLoader);
    }

    assertThat(warnings).hasSize(1);
    assertThat(warnings.get(0))
      .contains("does.not.Exist")
      .contains("META-INF/services/" + InjectExtension.class.getName());
  }

  @Test
  void loadExtensions_returnsEmptyWhenNoEntryLoads(@TempDir Path dir) throws Exception {
    try (var classLoader = classLoaderWithServices(dir, ClassLoader.getPlatformClassLoader(),
        "does.not.Exist",
        "also.does.not.Exist")) {

      assertThat(new AutoProvidesMojo().loadExtensions(classLoader)).isEmpty();
    }
  }

  @Test
  void loadExtensions_skipsProviderThatIsNotASubtype(@TempDir Path dir) throws Exception {
    final var mojo = new AutoProvidesMojo();
    final var warnings = capturingLog(mojo);

    try (var classLoader = classLoaderWithServices(dir, getClass().getClassLoader(),
        NotAnExtension.class.getName(),
        TestExtension.class.getName())) {

      assertThat(mojo.loadExtensions(classLoader)).hasExactlyElementsOfTypes(TestExtension.class);
    }

    assertThat(warnings).hasSize(1);
    assertThat(warnings.get(0)).contains(NotAnExtension.class.getName());
  }

  @Test
  void loadExtensions_skipsProviderWhoseConstructorThrows(@TempDir Path dir) throws Exception {
    final var mojo = new AutoProvidesMojo();
    final var warnings = capturingLog(mojo);

    try (var classLoader = classLoaderWithServices(dir, getClass().getClassLoader(),
        BrokenExtension.class.getName(),
        TestExtension.class.getName())) {

      assertThat(mojo.loadExtensions(classLoader)).hasExactlyElementsOfTypes(TestExtension.class);
    }

    assertThat(warnings).hasSize(1);
    assertThat(warnings.get(0)).contains(BrokenExtension.class.getName());
  }

  private List<String> capturingLog(AutoProvidesMojo mojo) {
    final List<String> warnings = new ArrayList<>();
    mojo.setLog(new SystemStreamLog() {
      @Override
      public void warn(CharSequence content) {
        warnings.add(content.toString());
      }
    });
    return warnings;
  }

  private URLClassLoader classLoaderWithServices(Path dir, ClassLoader parent, String... lines) throws Exception {
    final Path services = dir.resolve("META-INF/services/" + InjectExtension.class.getName());
    Files.createDirectories(services.getParent());
    Files.write(services, List.of(lines));
    return new URLClassLoader(new URL[]{dir.toUri().toURL()}, parent);
  }
}
