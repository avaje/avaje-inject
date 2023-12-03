package io.avaje.inject.generator;

import static io.avaje.inject.generator.APContext.createSourceFile;
import static java.util.function.Predicate.not;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.lang.model.element.Element;
import javax.tools.JavaFileObject;

import io.avaje.inject.generator.MethodReader.MethodParam;

/** Write the source code for the bean. */
final class SimpleAssistWriter {

  private static final String CODE_COMMENT =
      "/**\n * Generated source - dependency injection builder for %s.\n */";
  private static final String CODE_COMMENT_BUILD = "  /**\n   * Create and register %s.\n   */";
  private final AssistBeanReader beanReader;
  private final String originName;
  private final String shortName;
  private final String packageName;
  private final String suffix;
  private Append writer;
  private final List<Element> assistedElements;

  private final String indent = "   ";

  SimpleAssistWriter(AssistBeanReader beanReader) {
    this.beanReader = beanReader;
    this.packageName = beanReader.packageName();
    this.shortName = beanReader.shortName();
    this.suffix = "$Assist";
    this.assistedElements = beanReader.assistElements();
    this.originName = packageName + "." + shortName;
  }

  private Writer createFileWriter() throws IOException {
    String originName = this.originName;
    if (beanReader.beanType().getNestingKind().isNested()) {
      originName = originName.replace(shortName, shortName.replace(".", "$"));
    }
    final JavaFileObject jfo = createSourceFile(originName + suffix);
    return jfo.openWriter();
  }

  void write() throws IOException {
    writer = new Append(createFileWriter());
    writePackage();
    writeImports();
    writeClassStart();

    writeInjectFields();
    writeMethodFields();

    writeConstructor();

    writeBuildMethodStart();

    beanReader.injectMethods().forEach(this::writeInjectionMethods);
    writeClassEnd();
    writer.close();
  }

  private void writePackage() {
    if (packageName != null) {
      writer.append("package %s;", packageName).eol().eol();
    }
  }

  private void writeImports() {
    beanReader.writeImports(writer);
  }

  private void writeClassStart() {

    writer.append(CODE_COMMENT, shortName).eol();

    writer.append(beanReader.generatedType()).append(Constants.AT_GENERATED_COMMENT).eol();

    String name = this.shortName;
    if (beanReader.beanType().getNestingKind().isNested()) {
      name = name.replace(".", "$");
    }

    writer.append("@Component").eol();
    writer
        .append("public final class ")
        .append(name)
        .append(suffix)
        .append(" implements AssistInjector<%s>", shortName);

    writer.append(" {").eol().eol();
  }

  private void writeInjectFields() {

    if (beanReader.injectFields().isEmpty()) return;

    for (final var field : beanReader.injectFields()) {
      if (field.assisted()) {
        continue;
      }
      var element = field.element();
      AnnotationCopier.copyAnnotations(writer, element, "  ", true);
      var type = GenericType.parse(element.asType().toString());
      writer.append("  %s %s$field;", type.shortName(), field.fieldName()).eol().eol();
    }
    writer.eol();
  }

  private void writeMethodFields() {
    if (beanReader.injectMethods().isEmpty()) return;

    beanReader.injectMethods().stream()
        .flatMap(m -> m.params().stream())
        .filter(not(MethodParam::assisted))
        .forEach(
            p -> {
              var element = p.element();
              var type = GenericType.parse(element.asType().toString());

              writer
                  .append("  private %s %s$method;", type.shortName(), p.simpleName())
                  .eol()
                  .eol();
            });
    writer.eol();
  }

  private void writeConstructor() {
    if (beanReader.constructor().params().isEmpty()) {
      return;
    }
    beanReader.constructor().params().stream()
        .filter(not(MethodParam::assisted))
        .forEach(
            p -> {
              var element = p.element();
              var type = GenericType.parse(element.asType().toString());

              writer.append("  private final %s %s;", type.shortName(), p.simpleName()).eol().eol();
            });

    String shortName = this.shortName;
    if (beanReader.beanType().getNestingKind().isNested()) {
      shortName = shortName.replace(".", "$");
    }

    writer.append("  ").append(shortName).append(suffix).append("(");

    for (var iterator = beanReader.constructor().params().iterator(); iterator.hasNext(); ) {
      var p = iterator.next();
      if (p.assisted()) {
        continue;
      }

      var element = p.element();
      AnnotationCopier.copyAnnotations(writer, element, false);

      var type = GenericType.parse(element.asType().toString());

      writer.append("%s %s", type.shortName(), p.simpleName());
      if (iterator.hasNext()) {
        writer.append(",");
      }
    }

    writer.append(") {").eol();

    for (var p : beanReader.constructor().params()) {

      if (p.assisted()) {
        continue;
      }

      writer.append("    ").append("this.%s = %s;", p.simpleName(), p.simpleName()).eol();
    }

    writer.append("  }").eol().eol();
  }

  private void writeBuildMethodStart() {
    writer.append(CODE_COMMENT_BUILD, shortName).eol();

    writer.append("  @Override").eol();
    writer.append("  public %s inject(Object... deps) {", shortName).eol();
    var assists = beanReader.assistElements();
    var size = assists.size();
    writer
        .append(
            "    if(deps.length != %s) throw new IllegalArgumentException(\"expected %s arguments but recieved \" + deps.length);",
            size, size)
        .eol();

    for (int i = 0; i < size; i++) {
      var assist = assists.get(i);
      var type = GenericType.parse(assist.asType().toString());

      writer.append("    var arg%s = (%s) deps[%s];", i, type.shortName(), i).eol();
    }

    MethodReader constructor = beanReader.constructor();
    constructor.startTry(writer);

    writeCreateBean(constructor);
    beanReader.buildRegister(writer);
    if (beanReader.isExtraInjectionRequired()) {
      writeExtraInjection();
    }
    constructor.endTry(writer);
    writer.eol().append("  }").eol();
  }

  private void writeCreateBean(MethodReader constructor) {
    writer.indent(indent).append(" var bean = new %s(", shortName);
    // add constructor dependencies
    writeMethodParams(constructor, true);
  }

  private void writeExtraInjection() {

    injectFields();
    injectMethods();
    final var needsTry = beanReader.needsTryForMethodInjection();
    final var indent = needsTry ? "        " : "    ";

    writer.append(indent).indent("return bean;");
  }

  private void injectFields() {
    if (beanReader.injectFields().isEmpty()) return;
    for (FieldReader fieldReader : beanReader.injectFields()) {
      if (fieldReader.assisted()) {
        continue;
      }
      String fieldName = fieldReader.fieldName();
      String getDependency = fieldName + "$field";
      writer.indent("").append("bean.%s = %s;", fieldName, getDependency).eol();
    }

    for (int i = 0; i < assistedElements.size(); i++) {
      var field = assistedElements.get(i);
      String fieldName = field.getSimpleName().toString();
      writer.indent("    ").append("bean.%s = arg%s;", fieldName, i).eol();
    }
  }

  private void injectMethods() {
    final var needsTry = beanReader.needsTryForMethodInjection();

    if (needsTry) {
      writer.indent("        try {").eol();
    }
    final var indent = needsTry ? "    " : "  ";
    for (MethodReader methodReader : beanReader.injectMethods()) {
      writer.indent(indent).append("bean.%s(", methodReader.name());
      writeMethodParams(methodReader, false);
    }
    if (needsTry) {
      writer.indent("        } catch (Throwable e) {").eol();
      writer.indent("          throw new RuntimeException(\"Error wiring method\", e);").eol();
      writer.indent("        }").eol();
    }
  }

  private void writeMethodParams(MethodReader methodReader, boolean constructor) {
    var methodParams = methodReader.params();
    for (int i = 0; i < methodParams.size(); i++) {
      if (i > 0) {
        writer.append(", ");
      }
      final var methodParam = methodParams.get(i);
      if (!methodParam.assisted()) {
        writer.append(methodParam.simpleName()).append(constructor ? "" : "$method");
      } else {
        var index = getIndexByProperty(methodParam.element(), methodParam.simpleName());
        writer.append("arg%s", index);
      }
    }
    writer.append(");").eol();
  }

  private void writeClassEnd() {
    writer.append("}").eol();
  }

  private void writeInjectionMethods(MethodReader reader) {
    String simpleName = reader.name();
    String returnType = GenericType.parse(reader.element().getReturnType().toString()).shortName();
    writer.append("  public ").append(returnType).append(" ").append(simpleName).append("(");

    for (var iterator = reader.params().iterator(); iterator.hasNext(); ) {
      var p = iterator.next();
      if (p.assisted()) {
        continue;
      }

      var element = p.element();
      AnnotationCopier.copyAnnotations(writer, element, false);

      var type = GenericType.parse(element.asType().toString());

      writer.append("%s %s", type.shortName(), p.simpleName());
      if (iterator.hasNext()) {
        writer.append(",");
      }
    }

    writer.append(") {").eol();

    for (var p : reader.params()) {

      if (p.assisted()) {
        continue;
      }

      writer.append("this.%s$method = %s;", p.simpleName(), p.simpleName());
    }

    writer.append("  }").eol();
  }

  private int getIndexByProperty(Element element, String yourString) {
    for (int i = 0; i < assistedElements.size(); i++) {
      var e = assistedElements.get(i);
      if (element.getKind() == e.getKind() && e.getSimpleName().toString().equals(yourString)) {
        return i;
      }
    }
    return -1;
  }
}
