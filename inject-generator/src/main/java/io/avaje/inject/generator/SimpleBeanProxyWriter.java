package io.avaje.inject.generator;

import static io.avaje.inject.generator.APContext.createSourceFile;

import java.io.IOException;
import java.io.Writer;

import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

final class SimpleBeanProxyWriter {

  private final BeanReader beanReader;
  private final String originName;
  private final String suffix;
  private final String shortName;
  private final String packageName;
  private final BeanAspects aspects;
  private Append writer;

  SimpleBeanProxyWriter(BeanReader beanReader) {
    this.beanReader = beanReader;

    TypeElement origin = beanReader.beanType();
    this.originName = origin.getQualifiedName().toString();
    this.shortName = origin.getSimpleName().toString();
    this.packageName = ProcessorUtils.packageOf(originName);
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
    for (AspectMethod method : aspects.methods()) {
      method.writeSetupFields(writer);
    }
    writer.eol();
  }

  private void writeConstructor() {
    writer.append("  public %s%s(", shortName, suffix);
    int count = 0;
    for (final String aspectName : aspects.aspectNames()) {
      if (count++ > 0) {
        writer.append(", ");
      }
      final var type = "AspectProvider<" + aspectName + ">";
      final var name = Util.initLower(aspectName);
      writer.append(type).append(" ").append(name);
    }
    beanReader.writeConstructorParams(writer);
    writer.append(") {").eol();
    beanReader.writeConstructorInit(writer);
    writeSetupForMethods();
    writer.append("  }").eol();
  }

  private void writeSetupForMethods() {
    writer.append("    try {").eol();
    writer.append("      var target$Class = %s.class;", shortName).eol();
    for (AspectMethod method : aspects.methods()) {
      method.writeSetupForMethods(writer);
    }
    writer.append("    } catch (Exception e) {").eol();
    writer.append("      throw new IllegalStateException(e);").eol();
    writer.append("    }").eol();
  }

  private void writePackage() {
    if (packageName != null) {
      writer.append("package %s;", packageName).eol().eol();
    }
  }

  private void writeImports() {
    writer.append("import %s;", Constants.REFLECT_METHOD).eol();
    writer.append("import %s;", Constants.INVOCATION).eol();
    writer.append("import %s;", Constants.INVOCATION_EXCEPTION).eol();
    writer.append("import %s;", Constants.METHOD_INTERCEPTOR).eol();
    writer.append("import %s;", Constants.PROXY).eol();
    beanReader.writeImports(writer);
  }

  private void writeClassEnd() {
    writer.append("}").eol();
  }

  private void writeClassStart() {
    writer.append(Constants.AT_PROXY).eol();
    writer.append(Constants.AT_GENERATED).eol();
    writer.append("public final class %s%s extends %s {", shortName, suffix, shortName).eol().eol();
  }

  private Writer createFileWriter() throws IOException {
    JavaFileObject jfo = createSourceFile(originName + suffix);
    return jfo.openWriter();
  }

}
