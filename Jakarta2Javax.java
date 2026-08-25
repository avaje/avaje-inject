import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

public class Jakarta2Javax {

  public static void main(String[] args) {
    try {
      // Update version in every pom.
      // inject-bom is handled separately below because every version tag in it needs updating.
      Path bomPom = Paths.get("inject-bom/pom.xml").toAbsolutePath().normalize();
      for (Path pomFile : findPomFiles()) {
        if (!pomFile.equals(bomPom)) {
          updateFirstVersionTag(pomFile);
        }
      }

      // Replace specific version in inject/pom.xml
      replaceVersion(
          "inject/pom.xml",
          "<version>2.0.1</version> <!-- jakarta -->",
          "<version>1.0.5</version> <!-- javax -->");

      // Update inject BOM pom file - add -javax to all version tags
      updateInjectBomVersions("inject-bom/pom.xml");

      // Update module-info.java
      replaceInFile("inject/src/main/java/module-info.java", " jakarta.inject", " java.inject");

      // Update all Java files except IncludeAnnotations.java
      int updatedCount = updateJavaFiles();
      System.out.println("Total Java files updated: " + updatedCount);

    } catch (Exception e) {
      System.err.println("Error during conversion: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static List<Path> findPomFiles() throws IOException {
    Path currentDir = Paths.get(".").toAbsolutePath().normalize();
    try (Stream<Path> paths = Files.walk(currentDir)) {
      return paths
          .filter(Files::isRegularFile)
          .filter(p -> p.getFileName().toString().equals("pom.xml"))
          .filter(Jakarta2Javax::notBuildOutput)
          .sorted()
          .collect(Collectors.toList());
    }
  }

  /** Skip build output directories, which can hold stale copies of pom and java files. */
  private static boolean notBuildOutput(Path path) {
    for (Path part : path) {
      String name = part.toString();
      if (name.equals("target") || name.equals("bin")) {
        return false;
      }
    }
    return true;
  }

  private static void updateFirstVersionTag(Path path) throws IOException {
    if (!Files.exists(path)) {
      return;
    }

    String content = new String(Files.readAllBytes(path));

    // Pattern to match first <version>...</version>
    // Matches: <version>1.0</version> or <version>1.0-SNAPSHOT</version>
    Pattern pattern = Pattern.compile("<version>([^-<]+)(-)?([^<]*)</version>");
    Matcher matcher = pattern.matcher(content);

    if (matcher.find()) {
      String version = matcher.group(1);
      String existingDash = matcher.group(2);
      String suffix = matcher.group(3);

      // Build replacement - add -javax before any existing suffix
      String replacement;
      if (existingDash != null && !suffix.isEmpty()) {
        replacement = "<version>" + version + "-javax-" + suffix + "</version>";
      } else {
        replacement = "<version>" + version + "-javax" + "</version>";
      }

      content =
          content.substring(0, matcher.start()) + replacement + content.substring(matcher.end());

      Files.write(path, content.getBytes());
    }
  }

  private static void updateInjectBomVersions(String filePath) throws IOException {
    Path path = Paths.get(filePath);
    if (!Files.exists(path)) {
      return;
    }

    String content = new String(Files.readAllBytes(path));
    
    // Pattern to match all <version>...</version> tags
    // Matches versions like: 12.3, 1.0.5, 2.0.1, etc.
    Pattern pattern = Pattern.compile("<version>([0-9]+\\.[0-9]+(?:\\.[0-9]+)?)(?:-javax)?(?:-([^<]+))?</version>");
    StringBuffer result = new StringBuffer();
    Matcher matcher = pattern.matcher(content);
    
    while (matcher.find()) {
      String version = matcher.group(1);
      String suffix = matcher.group(2);
      
      // Build replacement - add -javax, preserve any additional suffix
      String replacement;
      if (suffix != null && !suffix.isEmpty()) {
        replacement = "<version>" + version + "-javax-" + suffix + "</version>";
      } else {
        replacement = "<version>" + version + "-javax</version>";
      }
      
      matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(result);
    
    Files.write(path, result.toString().getBytes());
    System.out.println("Updated inject BOM file: " + filePath);
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

    try (Stream<Path> paths = Files.walk(currentDir)) {
      paths
          .filter(Files::isRegularFile)
          .filter(p -> p.toString().endsWith(".java"))
          .filter(Jakarta2Javax::notBuildOutput)
          .filter(
              p ->
                  !p.getFileName().toString().equals("IncludeAnnotations.java")
                      && !p.getFileName().toString().equals("Javax2Jakarta.java")
                      && !p.getFileName().toString().equals("Jakarta2Javax.java"))
          .forEach(
              path -> {
                try {
                  String content = new String(Files.readAllBytes(path));
                  String updated = content.replace("jakarta.inject.", "javax.inject.");

                  if (!content.equals(updated)) {
                    Files.write(path, updated.getBytes());
                    count[0]++;
                  }
                } catch (IOException e) {
                  System.err.println("  Error updating " + path + ": " + e.getMessage());
                }
              });
    }

    return count[0];
  }
}