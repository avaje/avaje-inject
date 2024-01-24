package io.avaje.inject.generator;

import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;

final class AnnotationCopier {
  private AnnotationCopier() {}

  public static void copyAnnotations(Append writer, Element element, boolean newLines) {
    copyAnnotations(writer, element, "", newLines);
  }

  public static void copyAnnotations(Append writer, Element element, String indent, boolean newLines) {
    for (final AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      final var type = annotationMirror.getAnnotationType().asElement().asType().toString();
      if (type.startsWith("io.avaje.inject.Assist")) {
        continue;
      }
      final String annotationName = annotationMirror.getAnnotationType().toString();
      final StringBuilder sb =  new StringBuilder(indent).append("@").append(annotationName).append("(");
      boolean first = true;

      for (final var entry : annotationMirror.getElementValues().entrySet()) {
        if (!first) {
          sb.append(", ");
        }
        sb.append(entry.getKey().getSimpleName()).append("=");
        writeVal(sb, entry.getValue());
        first = false;
      }

      sb.append(")");
      final String annotationString = sb.toString();
      writer.append(annotationString);

      if (newLines) {
        writer.eol();
      } else {
        writer.append(" ");
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static void writeVal(final StringBuilder sb, final AnnotationValue annotationValue) {
    final var value = annotationValue.getValue();
    if (value instanceof List) {
      // handle array values
      sb.append("{");
      boolean first = true;
      for (final AnnotationValue listValue : (List<AnnotationValue>) value) {
        if (!first) {
          sb.append(", ");
        }
        writeVal(sb, listValue);
        first = false;
      }
      sb.append("}");

    } else if (value instanceof VariableElement) {
      // Handle enum values
      final var element = (VariableElement) value;
      final var type = element.asType();
      sb.append(type.toString() + "." + element);

    } else if (value instanceof AnnotationMirror) {
      // handle annotation values
      final var mirror = (AnnotationMirror) value;
      final String annotationName = mirror.getAnnotationType().toString();
      sb.append("@").append(annotationName).append("(");
      boolean first = true;

      for (final var entry : mirror.getElementValues().entrySet()) {
        if (!first) {
          sb.append(", ");
        }
        sb.append(entry.getKey().getSimpleName()).append("=");
        writeVal(sb, entry.getValue());
        first = false;
      }
      sb.append(")");

    } else {
      sb.append(annotationValue);
    }
  }
}
