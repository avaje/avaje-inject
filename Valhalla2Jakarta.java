import java.io.*;
import java.nio.file.*;
import java.util.stream.*;

public class Valhalla2Jakarta {

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
          .filter(p -> !p.getFileName().toString().equals("Valhalla2Jakarta.java"))
          .forEach(
              path -> {
                try {
                  String content = new String(Files.readAllBytes(path), "UTF-8");
                  String updated = content.replace(" value class ", " /*value*/ class ");
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
    Path jvmConfig = Paths.get(".mvn/jvm.config");
    Path jvmConfigDisabled = Paths.get(".mvn/jvm.config.disabled");

    if (Files.exists(jvmConfig)) {
      Files.move(jvmConfig, jvmConfigDisabled, StandardCopyOption.REPLACE_EXISTING);
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

                  content = content.replace("--enable-preview -Dnet.bytebuddy", "-Dnet.bytebuddy");
                  content = content.replace("<!-- VALHALLA-START -->", "<!-- VALHALLA-START ___");
                  content = content.replace("<!-- VALHALLA-END -->", "____ VALHALLA-END -->");
                  content =
                      content.replace(
                          "<additionalOptions>--enable-preview</additionalOptions> <!-- Valhalla -->",
                          "<!-- Javadoc-No-Preview -->");

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
    Path testFile =
        Paths.get(
            "./inject-generator/src/test/java/io/avaje/inject/generator/InjectProcessorTest.java");

    if (!Files.exists(testFile)) {
      return;
    }

    String content = new String(Files.readAllBytes(testFile), "UTF-8");
    String updated = content.replace("@Disabled", "//@Disabled");

    if (!content.equals(updated)) {
      Files.write(testFile, updated.getBytes("UTF-8"));
    }
  }
}
