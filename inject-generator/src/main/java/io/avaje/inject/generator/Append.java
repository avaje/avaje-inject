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
  private String indent = "      ";

  Append(Writer writer) {
    this.writer = writer;
  }

  /** Increase the current indentation */
  Append incIndent() {
    this.indent += "  ";
    return this;
  }

  /** Reduce the current indentation */
  Append decIndent() {
    this.indent = indent.substring(0, indent.length() - 2);
    return this;
  }

  /** start a NEW line of content using the current indentation */
  Append start(String format, Object... args) {
    return start(String.format(format, args));
  }

  /** start a NEW line of content using the current indentation */
  Append start(String content) {
    try {
      writer.append(indent).append(content);
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
