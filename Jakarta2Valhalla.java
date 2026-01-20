import java.io.*;
import java.nio.file.*;
import java.util.stream.*;

public class Jakarta2Valhalla {

  public static void main(String[] args) {
    try {
      updateValueClassInJavaFiles();
      renameJvmConfig();
      updatePomFiles();
      updateInjectProcessorTest();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void updateValueClassInJavaFiles() throws IOException {
    Path currentDir = Paths.get(".").toAbsolutePath().normalize();

    try (Stream<Path> paths = Files.walk(currentDir)) {
      paths
          .filter(Files::isRegularFile)
          .filter(p -> p.toString().endsWith(".java"))
          .filter(p -> !p.getFileName().toString().equals("Jakarta2Valhalla.java"))
          .forEach(
              path -> {
                try {
                  String content = new String(Files.readAllBytes(path), "UTF-8");
                  String updated = content.replace(" /*value*/ class ", " value class ");
                  if (!content.equals(updated)) {
                    Files.write(path, updated.getBytes("UTF-8"));
                  }
                } catch (IOException e) {
                  e.printStackTrace();
                }
              });
    }
  }

  private static void renameJvmConfig() throws IOException {
    Path jvmConfigDisabled = Paths.get(".mvn/jvm.config.disabled");
    Path jvmConfig = Paths.get(".mvn/jvm.config");

    if (Files.exists(jvmConfigDisabled)) {
      Files.move(jvmConfigDisabled, jvmConfig, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  private static void updatePomFiles() throws IOException {
    Path currentDir = Paths.get(".").toAbsolutePath().normalize();

    try (Stream<Path> paths = Files.walk(currentDir)) {
      paths
          .filter(Files::isRegularFile)
          .filter(p -> p.getFileName().toString().equals("pom.xml"))
          .forEach(
              path -> {
                try {
                  String content = new String(Files.readAllBytes(path), "UTF-8");
                  String original = content;

                  content = content.replace("-Dnet.bytebuddy", "--enable-preview -Dnet.bytebuddy");
                  content =
                      content.replace(
                          "<!-- default-build-start -->", "<!-- default-build-start ___");
                  content =
                      content.replace("<!-- default-build-end -->", "____ default-build-end -->");
                  content =
                      content.replace(
                          "<!-- valhalla-build-start ___", "<!-- valhalla-build-start -->");
                  content =
                      content.replace("____ valhalla-build-end -->", "<!-- valhalla-build-end -->");
                  content =
                      content.replace(
                          "<!-- Javadoc-No-Preview -->",
                          "<additionalOptions>--enable-preview</additionalOptions> <!-- Valhalla -->");

                  if (!original.equals(content)) {
                    Files.write(path, content.getBytes("UTF-8"));
                  }
                } catch (IOException e) {
                  e.printStackTrace();
                }
              });
    }
  }

  private static void updateInjectProcessorTest() throws IOException {
    Path currentDir = Paths.get(".").toAbsolutePath().normalize();

    try (Stream<Path> paths = Files.walk(currentDir)) {
      paths
          .filter(Files::isRegularFile)
          .filter(p -> p.getFileName().toString().equals("InjectProcessorTest.java"))
          .forEach(
              path -> {
                try {
                  String content = new String(Files.readAllBytes(path), "UTF-8");
                  String updated =
                      content.replace("//@Disabled", "@org.junit.jupiter.api.Disabled // Valhalla");

                  if (!content.equals(updated)) {
                    Files.write(path, updated.getBytes("UTF-8"));
                  }
                } catch (IOException e) {
                  e.printStackTrace();
                }
              });
    }
  }
}
