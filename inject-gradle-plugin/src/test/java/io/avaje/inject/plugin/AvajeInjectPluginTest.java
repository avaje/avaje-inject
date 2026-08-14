package io.avaje.inject.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.avaje.inject.spi.InjectExtension;

class AvajeInjectPluginTest {

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

      assertThat(AvajeInjectPlugin.loadExtensions(classLoader))
        .hasExactlyElementsOfTypes(TestExtension.class, OtherTestExtension.class);
    }
  }

  @Test
  void loadExtensions_returnsEmptyWhenNoEntryLoads(@TempDir Path dir) throws Exception {
    try (var classLoader = classLoaderWithServices(dir, ClassLoader.getPlatformClassLoader(),
        "does.not.Exist",
        "also.does.not.Exist")) {

      assertThat(AvajeInjectPlugin.loadExtensions(classLoader)).isEmpty();
    }
  }

  @Test
  void loadExtensions_skipsProviderThatIsNotASubtype(@TempDir Path dir) throws Exception {
    try (var classLoader = classLoaderWithServices(dir, getClass().getClassLoader(),
        NotAnExtension.class.getName(),
        TestExtension.class.getName())) {

      assertThat(AvajeInjectPlugin.loadExtensions(classLoader))
        .hasExactlyElementsOfTypes(TestExtension.class);
    }
  }

  @Test
  void loadExtensions_skipsProviderWhoseConstructorThrows(@TempDir Path dir) throws Exception {
    try (var classLoader = classLoaderWithServices(dir, getClass().getClassLoader(),
        BrokenExtension.class.getName(),
        TestExtension.class.getName())) {

      assertThat(AvajeInjectPlugin.loadExtensions(classLoader))
        .hasExactlyElementsOfTypes(TestExtension.class);
    }
  }

  private URLClassLoader classLoaderWithServices(Path dir, ClassLoader parent, String... lines) throws Exception {
    final Path services = dir.resolve("META-INF/services/" + InjectExtension.class.getName());
    Files.createDirectories(services.getParent());
    Files.write(services, List.of(lines));
    return new URLClassLoader(new URL[]{dir.toUri().toURL()}, parent);
  }
}
