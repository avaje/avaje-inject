package io.avaje.inject.test;

import java.util.Collections;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class AnnotationReader {
  private AnnotationReader() {
  }

  private static final Pattern ANNOTATION_TYPE_PATTERN = Pattern.compile("@([\\w.]+)\\.");

  private static final Pattern ANNOTATION_TYPE_PATTERN$ = Pattern.compile("@[\\w.]+\\$");

  private static final Pattern VALUE_ATTRIBUTE_PATTERN = Pattern.compile("(\\w+)=([\\w.]+)");

  static String simplifyAnnotation(String input) {
    String result = ANNOTATION_TYPE_PATTERN$.matcher(input).replaceAll("@");
    result = ANNOTATION_TYPE_PATTERN.matcher(result).replaceAll("@");

    Matcher valueAttributeMatcher = VALUE_ATTRIBUTE_PATTERN.matcher(result);

    // Replace package names in any attribute value with only the class name
    StringBuilder sb = new StringBuilder();
    while (valueAttributeMatcher.find()) {
      valueAttributeMatcher.appendReplacement(
        sb, valueAttributeMatcher.group(1) + "=" + shortType(valueAttributeMatcher.group(2)));
    }
    valueAttributeMatcher.appendTail(sb);
    return rearrangeAnnotations(sb.toString());
  }

  static String shortType(String fqn) {
    final int dotIndex = fqn.lastIndexOf('.');
    final int $index = fqn.lastIndexOf('$');

    if (dotIndex == -1 && $index == -1) {
      return fqn;
    } else if ($index != -1) {
      return fqn.substring($index + 1);
    }
    return fqn.substring(dotIndex + 1);
  }

  static String rearrangeAnnotations(String input) {
    final var indexOfParanthesis = input.indexOf('(');
    if (indexOfParanthesis == -1) {
      return input;
    }
    // Extracting annotation content
    var annotationContent = input.substring(indexOfParanthesis + 1, input.lastIndexOf(')'));

    // Splitting annotations
    String[] annotations = annotationContent.split(",\\s*");

    // Creating a TreeMap to store annotations sorted by key
    var sortedkeys = new TreeSet<String>();

    // Sorting by key
    Collections.addAll(sortedkeys, annotations);

    // Constructing the sorted annotation string
    StringBuilder sortedOutput = new StringBuilder(input.substring(0, indexOfParanthesis + 1));
    for (var key : sortedkeys) {
      sortedOutput.append(key).append(", ");
    }
    // Removing the trailing comma and space
    sortedOutput.setLength(sortedOutput.length() - 2);
    sortedOutput.append(")");
    return sortedOutput.toString();
  }
}
