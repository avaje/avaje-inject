package io.avaje.inject.generator;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map.Entry;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

final class AnnotationCopier {
  private AnnotationCopier() {}

  public static void copyAnnotations(Append writer, Element element, boolean newLines) {
    copyAnnotations(writer, element, "", newLines);
  }

  public static void copyAnnotations(
      Append writer, Element element, String indent, boolean newLines) {
    for (final AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      final var type = annotationMirror.getAnnotationType().asElement().asType().toString();
      if (type.startsWith("io.avaje.inject.Assist")) {
        continue;
      }

      final String annotationString = getAnnotationString(indent, annotationMirror, false);
      writer.append(annotationString);

      if (newLines) {
        writer.eol();
      } else {
        writer.append(" ");
      }
    }
  }

  static String getSimpleAnnotationString(AnnotationMirror annotationMirror) {
    return Util.trimAnnotationString(getAnnotationString("", annotationMirror, true)).substring(1);
  }

  static String getAnnotationString(
      String indent, AnnotationMirror annotationMirror, boolean simpleEnums) {
    final String annotationName = annotationMirror.getAnnotationType().toString();

    final StringBuilder sb =
        new StringBuilder(indent).append("@").append(annotationName).append("(");
    boolean first = true;

    for (final var entry : sortedValues(annotationMirror)) {
      if (!first) {
        sb.append(", ");
      }
      sb.append(entry.getKey().getSimpleName()).append("=");
      writeVal(sb, entry.getValue(), simpleEnums);
      first = false;
    }

    return sb.append(")").toString().replace("()", "");
  }

  private static List<Entry<? extends ExecutableElement, ? extends AnnotationValue>> sortedValues(
      AnnotationMirror annotationMirror) {
    return APContext.elements().getElementValuesWithDefaults(annotationMirror).entrySet().stream()
        .sorted(
            (e1, e2) ->
                e1.getKey()
                    .getSimpleName()
                    .toString()
                    .compareTo(e2.getKey().getSimpleName().toString()))
        .collect(toList());
  }

  @SuppressWarnings("unchecked")
  private static void writeVal(
      final StringBuilder sb, final AnnotationValue annotationValue, boolean simpleEnums) {
    final var value = annotationValue.getValue();
    if (value instanceof List) {
      // handle array values
      sb.append("{");
      boolean first = true;
      for (final AnnotationValue listValue : (List<AnnotationValue>) value) {
        if (!first) {
          sb.append(", ");
        }
        writeVal(sb, listValue, simpleEnums);
        first = false;
      }
      sb.append("}");

    } else if (value instanceof VariableElement) {
      // Handle enum values
      final var element = (VariableElement) value;
      final var type = element.asType();
      final var str = simpleEnums ? element : type.toString() + "." + element;
      sb.append(str);

    } else if (value instanceof AnnotationMirror) {
      // handle annotation values
      final var mirror = (AnnotationMirror) value;
      final String annotationName = mirror.getAnnotationType().toString();
      sb.append("@").append(annotationName).append("(");
      boolean first = true;

      for (final var entry : sortedValues(mirror)) {
        if (!first) {
          sb.append(", ");
        }
        sb.append(entry.getKey().getSimpleName()).append("=");
        writeVal(sb, entry.getValue(), simpleEnums);
        first = false;
      }
      sb.append(")");

    } else {
      sb.append(annotationValue);
    }
  }
}
