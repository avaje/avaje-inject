package io.avaje.inject.mojo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ServiceConfigurationError;

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
        OtherTestExtension.class.getName())) {

      final var extensions = new AutoProvidesMojo().loadExtensions(classLoader);

      assertEquals(1, extensions.stream().filter(TestExtension.class::isInstance).count());
      assertEquals(1, extensions.stream().filter(OtherTestExtension.class::isInstance).count());
    }
  }

  @Test
  void loadExtensions_returnsEmptyWhenNoEntryLoads(@TempDir Path dir) throws Exception {
    try (var classLoader = classLoaderWithServices(dir, ClassLoader.getPlatformClassLoader(),
        "does.not.Exist",
        "also.does.not.Exist")) {

      assertTrue(new AutoProvidesMojo().loadExtensions(classLoader).isEmpty());
    }
  }

  @Test
  void loadExtensions_failsWhenProviderIsNotASubtype(@TempDir Path dir) throws Exception {
    try (var classLoader = classLoaderWithServices(dir, getClass().getClassLoader(),
        NotAnExtension.class.getName())) {

      assertThrows(ServiceConfigurationError.class, () -> new AutoProvidesMojo().loadExtensions(classLoader));
    }
  }

  @Test
  void loadExtensions_failsWhenProviderConstructorThrows(@TempDir Path dir) throws Exception {
    try (var classLoader = classLoaderWithServices(dir, getClass().getClassLoader(),
        BrokenExtension.class.getName())) {

      assertThrows(ServiceConfigurationError.class, () -> new AutoProvidesMojo().loadExtensions(classLoader));
    }
  }

  private URLClassLoader classLoaderWithServices(Path dir, ClassLoader parent, String... lines) throws Exception {
    final Path services = dir.resolve("META-INF/services/" + InjectExtension.class.getName());
    Files.createDirectories(services.getParent());
    Files.write(services, List.of(lines));
    return new URLClassLoader(new URL[]{dir.toUri().toURL()}, parent);
  }
}
