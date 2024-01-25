package io.avaje.inject.test;

import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class AnnotationReader {

  private static final Pattern ANNOTATION_TYPE_PATTERN = Pattern.compile("@([\\w.]+)\\.");
  private static final Pattern ANNOTATION_TYPE_PATTERN$ = Pattern.compile("@[\\w.]+\\$");

  private static final Pattern VALUE_ATTRIBUTE_PATTERN = Pattern.compile("(\\w+)=([\\w.]+)");

  public static String simplifyAnnotation(String input) {
    String result = ANNOTATION_TYPE_PATTERN$.matcher(input).replaceAll("@");

    result = ANNOTATION_TYPE_PATTERN.matcher(result).replaceAll("@");

    Matcher valueAttributeMatcher = VALUE_ATTRIBUTE_PATTERN.matcher(result);

    // Replace package names in any attribute value with only the class name
    StringBuffer sb = new StringBuffer();
    while (valueAttributeMatcher.find()) {
      valueAttributeMatcher.appendReplacement(
          sb, valueAttributeMatcher.group(1) + "=" + shortType(valueAttributeMatcher.group(2)));
    }
    valueAttributeMatcher.appendTail(sb);
    return rearrangeAnnotations(sb.toString());
  }

  public static String shortType(String fqn) {
    final int dotIndex = fqn.lastIndexOf('.');
    final int $index = fqn.lastIndexOf('$');

    if (dotIndex == -1 && $index == -1) {
      return fqn;
    } else if ($index != -1) {

      return fqn.substring($index + 1);
    }
    return fqn.substring(dotIndex + 1);
  }

  public static String rearrangeAnnotations(String input) {
    final var indexOfParanthesis = input.indexOf('(');
    if (indexOfParanthesis == -1) return input;

    // Extracting annotation content
    var annotationContent = input.substring(indexOfParanthesis + 1, input.lastIndexOf(')'));

    // Splitting annotations
    String[] annotations = annotationContent.split(",\\s*");

    // Creating a TreeMap to store annotations sorted by key
    Map<String, String> sortedAnnotations = new TreeMap<>();

    // Sorting annotations by key
    for (String annotation : annotations) {
      String[] keyValue = annotation.split("=",1);
      sortedAnnotations.put(keyValue[0], keyValue.length > 1 ? keyValue[1] : "");
    }

    // Constructing the sorted annotation string
    StringBuilder sortedOutput = new StringBuilder(input.substring(0, indexOfParanthesis+1));
    for (Map.Entry<String, String> entry : sortedAnnotations.entrySet()) {
      sortedOutput.append(entry.getKey());
      if (!entry.getValue().isEmpty()) {
        sortedOutput.append("=").append(entry.getValue());
      }
      sortedOutput.append(", ");
    }
    // Removing the trailing comma and space
    sortedOutput.setLength(sortedOutput.length() - 2);

    sortedOutput.append(")");

    return sortedOutput.toString();
  }
}
