package io.avaje.inject.generator;

class AspectTarget {

  private final AspectTargetReader reader;

  AspectTarget(AspectTargetReader reader) {
    this.reader = reader;
  }

  static String shortName(String target) {
    String type = Util.shortName(target);
    return Util.initLower(type);
  }

  void writeBefore(Append writer, AspectMethod aspectMethod) {
    reader.writeBefore(writer, aspectMethod);
  }

  void writeAfter(Append writer, AspectMethod aspectMethod) {
    reader.writeAfter(writer, aspectMethod);
  }
}
