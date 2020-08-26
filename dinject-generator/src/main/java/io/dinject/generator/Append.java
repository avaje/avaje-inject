package io.dinject.generator;

import java.io.IOException;
import java.io.Writer;

/**
 * Helper that wraps a writer with some useful methods to append content.
 */
class Append {

  private final Writer writer;
  private int nameIndex;
  private boolean comma;

  Append(Writer writer) {
    this.writer = writer;
  }

  Append append(String content) {
    try {
      writer.append(content);
      return this;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  void close() {
    try {
      writer.flush();
      writer.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  Append eol() {
    try {
      writer.append("\n");
      return this;
    } catch (IOException e) {
      throw new RuntimeException(e);
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
}
