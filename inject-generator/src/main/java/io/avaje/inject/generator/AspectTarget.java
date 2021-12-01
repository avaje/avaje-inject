package io.avaje.inject.generator;

class AspectTarget {

  private final AspectTargetReader reader;

  AspectTarget(AspectTargetReader reader) {
    this.reader = reader;
  }

  void writeBefore(Append writer) {
    reader.writeBefore(writer);
  }
}
