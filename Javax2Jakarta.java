import java.io.*;
import java.nio.file.*;
import java.util.regex.*;
import java.util.stream.*;

public class Javax2Jakarta {

  public static void main(String[] args) {
    try {
      // List of pom.xml files to update
      String[] pomFiles = {
        "pom.xml",
        "inject/pom.xml",
        "inject-aop/pom.xml",
        "inject-events/pom.xml",
        "inject-generator/pom.xml",
        "inject-maven-plugin/pom.xml",
        "inject-test/pom.xml",
        "blackbox-aspect/pom.xml",
        "blackbox-other/pom.xml",
        "blackbox-test-inject/pom.xml",
        "blackbox-multi-scope/pom.xml",
        "inject-bom/pom.xml"
      };

      // Remove -javax from version tags in pom files
      for (String pomFile : pomFiles) {
        removeJavaxFromVersion(pomFile);
      }

      // Replace specific version in inject/pom.xml (reverse operation)
      replaceVersion(
          "inject/pom.xml",
          "<version>1.0.5</version> <!-- javax -->",
          "<version>2.0.1</version> <!-- jakarta -->");

      // Update module-info.java (reverse operation)
      replaceInFile("inject/src/main/java/module-info.java", " java.inject", " jakarta.inject");

      // Update all Java files except IncludeAnnotations.java (reverse operation)
      int updatedCount = updateJavaFiles();

    } catch (Exception e) {
      System.err.println("Error during reversion: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static void removeJavaxFromVersion(String filePath) throws IOException {
    Path path = Paths.get(filePath);
    if (!Files.exists(path)) {
      return;
    }

    String content = new String(Files.readAllBytes(path));

    // Pattern to remove -javax from version tags
    // Matches: <version>1.0-javax</version> or <version>1.0-javax-SNAPSHOT</version>
    Pattern pattern = Pattern.compile("(<version>[^<]*)-javax([^<]*</version>)");
    Matcher matcher = pattern.matcher(content);

    StringBuffer result = new StringBuffer();
    boolean found = false;

    while (matcher.find()) {
      found = true;
      // Replace with version without -javax
      matcher.appendReplacement(result, "$1$2");
    }
    matcher.appendTail(result);

    if (found) {
      Files.write(path, result.toString().getBytes());
    }
  }

  private static void replaceVersion(String filePath, String search, String replace)
      throws IOException {
    Path path = Paths.get(filePath);
    if (!Files.exists(path)) {
      return;
    }

    String content = new String(Files.readAllBytes(path));
    String updated = content.replace(search, replace);

    if (!content.equals(updated)) {
      Files.write(path, updated.getBytes());
    }
  }

  private static void replaceInFile(String filePath, String search, String replace)
      throws IOException {
    Path path = Paths.get(filePath);
    if (!Files.exists(path)) {
      return;
    }

    String content = new String(Files.readAllBytes(path));
    String updated = content.replace(search, replace);

    if (!content.equals(updated)) {
      Files.write(path, updated.getBytes());
    }
  }

  private static int updateJavaFiles() throws IOException {
    Path currentDir = Paths.get(".").toAbsolutePath().normalize();
    int[] count = {0};
    int[] totalJavaFiles = {0};

    try (Stream<Path> paths = Files.walk(currentDir)) {
      paths
          .filter(Files::isRegularFile)
          .filter(p -> p.toString().endsWith(".java"))
          .filter(
              p ->
                  !p.getFileName().toString().equals("IncludeAnnotations.java")
                      && !p.getFileName().toString().equals("Javax2Jakarta.java")
                      && !p.getFileName().toString().equals("Jakarta2Javax.java"))
          .forEach(
              path -> {
                totalJavaFiles[0]++;
                try {
                  String content = new String(Files.readAllBytes(path), "UTF-8");
                  String updated = content.replace("javax.inject.", "jakarta.inject.");

                  if (!content.equals(updated)) {
                    Files.write(path, updated.getBytes("UTF-8"));
                    count[0]++;
                  }
                } catch (IOException e) {
                  System.err.println("Error updating " + path + ": " + e.getMessage());
                }
              });
    }
    return count[0];
  }
}