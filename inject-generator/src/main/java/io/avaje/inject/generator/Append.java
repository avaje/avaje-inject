package io.avaje.inject.generator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;

/**
 * Helper that wraps a writer with some useful methods to append content.
 */
final class Append {

  private static final boolean debug = Boolean.getBoolean("append.debug");

  private final Writer writer;
  private final StringBuilder stringBuilder = new StringBuilder();
  private int nameIndex;
  private boolean comma;
  private String extraIndent;

  Append(Writer writer) {
    this.writer = writer;
  }

  Append setExtraIndent(String extraIndent) {
    this.extraIndent = extraIndent;
    return this;
  }

  Append indent(String content) {
    try {
      if (extraIndent != null) {
        writer.append(extraIndent);
        if (debug) {
          stringBuilder.append(extraIndent);
        }
      }
      writer.append(content);
      if (debug) {
        stringBuilder.append(content);
      }
      return this;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  Append append(String content) {
    try {
      writer.append(content);
      if (debug) {
        stringBuilder.append(content);
      }
      return this;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  void close() {
    try {
      writer.flush();
      writer.close();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  Append eol() {
    try {
      writer.append("\n");
      if (debug) {
        stringBuilder.append("\n");
      }
      return this;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Append content with formatted arguments.
   */
  Append append(String format, Object... args) {
    return append(String.format(format, args));
  }

  void resetNextName() {
    nameIndex = 0;
    comma = false;
  }

  String nextName(String prefix) {
    return prefix + nameIndex++;
  }

  void commaAppend(String name) {
    if (!comma) {
      comma = true;
    } else {
      append(", ");
    }
    append(name);
  }


  @Override
  public String toString() {
    return stringBuilder.toString();
  }
}
