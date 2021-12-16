package io.avaje.inject.generator;

import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;

class SimpleBeanProxyWriter {

  private final BeanReader beanReader;
  private final ProcessingContext context;
  private final String originName;
  private final String suffix;
  private final String shortName;
  private final String packageName;
  private final BeanAspects aspects;
  private Append writer;

  SimpleBeanProxyWriter(BeanReader beanReader, ProcessingContext context) {
    this.beanReader = beanReader;
    this.context = context;

    TypeElement origin = beanReader.getBeanType();
    this.originName = origin.getQualifiedName().toString();
    this.shortName = origin.getSimpleName().toString();
    this.packageName = Util.packageOf(originName);
    this.suffix = "$Proxy";
    this.aspects = beanReader.aspects();
  }

  void write() throws IOException {
    writer = new Append(createFileWriter());
    writePackage();
    writeImports();
    writeClassStart();
    writeFields();
    writeConstructor();
    writeMethods();
    writeClassEnd();
    writer.close();
  }

  private void writeMethods() {
    for (AspectMethod method : aspects.methods()) {
      method.writeMethod(writer);
    }
  }

  private void writeFields() {
    aspects.writeFields(writer);
  }

  private void writeConstructor() {
    writer.append("  public %s%s(", shortName, suffix);
    int count = 0;
    for (String target : aspects.targets()) {
      if (count++ > 0) {
        writer.append(", ");
      }
      final String type = Util.shortName(target);
      String name = Util.initLower(type);
      writer.append(type).append(" ").append(name);
    }
    beanReader.writeConstructorParams(writer);
    writer.append(") {").eol();
    beanReader.writeConstructorInit(writer);
    for (String target : aspects.targets()) {
      String type = Util.shortName(target);
      String name = Util.initLower(type);
      writer.append("    this.%s = %s;", name, name).eol();
    }
    writer.append("  }").eol();
  }

  private void writePackage() {
    if (packageName != null) {
      writer.append("package %s;", packageName).eol().eol();
    }
  }

  private void writeImports() {
    beanReader.writeImports(writer);
  }

  private void writeClassEnd() {
    writer.append("}").eol();
  }

  private void writeClassStart() {
    writer.append(Constants.AT_GENERATED).eol();
    writer.append("public class %s%s extends %s {", shortName, suffix, shortName).eol().eol();
  }

  private Writer createFileWriter() throws IOException {
    JavaFileObject jfo = context.createWriter(originName + suffix);
    return jfo.openWriter();
  }

}
