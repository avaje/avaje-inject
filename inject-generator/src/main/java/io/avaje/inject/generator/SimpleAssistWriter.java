package io.avaje.inject.generator;

import io.avaje.inject.generator.MethodReader.MethodParam;

import javax.lang.model.element.*;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;

import static io.avaje.inject.generator.APContext.createSourceFile;
import static java.util.function.Predicate.not;

/**
 * Write the source code for the bean.
 */
final class SimpleAssistWriter {

  private static final String CODE_COMMENT = "/**\n * Generated source - Factory for %s.\n */";
  private static final String CODE_COMMENT_BUILD = "  /**\n   * Fabricates a new %s.\n   */";
  private final AssistBeanReader beanReader;
  private final String originName;
  private final String shortName;
  private final String packageName;
  private final String suffix;
  private Append writer;
  private final List<Element> assistedElements;
  private final boolean hasNoConstructorParams;

  SimpleAssistWriter(AssistBeanReader beanReader) {
    this.beanReader = beanReader;
    this.packageName = beanReader.packageName();
    this.shortName = beanReader.shortName();
    this.suffix = "$AssistFactory";
    this.assistedElements = beanReader.assistElements();
    this.originName = packageName + "." + shortName;
    this.hasNoConstructorParams =
      beanReader.constructor().params().stream()
        .filter(not(MethodParam::assisted))
        .findAny()
        .isEmpty();
  }

  private Writer createFileWriter() throws IOException {
    String origin = this.originName;
    if (beanReader.beanType().getNestingKind().isNested()) {
      origin = origin.replace(shortName, shortName.replace(".", "$"));
    }
    final JavaFileObject jfo = createSourceFile(origin + suffix);
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
    writeFactoryMethod();
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
    beanReader.writeImports(writer, packageName);
  }

  private void writeClassStart() {
    writer.append(CODE_COMMENT, shortName).eol();
    writer.append(Constants.AT_GENERATED).eol();

    String name = this.shortName;
    if (beanReader.beanType().getNestingKind().isNested()) {
      name = name.replace(".", "$");
    }
    String qualifierName = beanReader.qualifierName();
    if (qualifierName != null) {
      writer.append("@Named(\"%s\")", qualifierName).eol();
    }
    writer.append("@Component").eol();
    if (!beanReader.hasTargetFactory()) {
      writer.append("public ");
    }
    var valhallaStr = Util.valhalla();
    if (!valhallaStr.isBlank() && hasAssistedFieldsOrParams()) {
      valhallaStr = "";
    }

    writer.append("final %sclass %s%s", valhallaStr, name, suffix);
    writeImplementsOrExtends();
    writer.append(" {").eol().eol();
  }

  private boolean hasAssistedFieldsOrParams() {
    return beanReader.injectFields().stream().anyMatch(FieldReader::assisted)
      || beanReader.injectMethods().stream()
      .flatMap(m -> m.params().stream())
      .anyMatch(MethodParam::assisted);
  }

  private void writeImplementsOrExtends() {
    TypeElement targetInterface = beanReader.targetInterface();
    writer
      .append(targetInterface.getKind() == ElementKind.INTERFACE ? " implements " : " extends ")
      .append(Util.shortName(targetInterface.getQualifiedName().toString()));
    if (!targetInterface.getTypeParameters().isEmpty()) {
      writer.append("<%s>", shortName);
    }
  }

  private void writeInjectFields() {
    if (beanReader.injectFields().isEmpty()) {
      return;
    }
    for (final var field : beanReader.injectFields()) {
      if (field.assisted()) {
        continue;
      }
      var element = field.element();
      AnnotationCopier.copyAnnotations(writer, element, "  ", true);
      var type = UType.parse(element.asType());
      writer.append("  %s %s$field;", type.shortType(), field.fieldName()).eol().eol();
    }
    if (beanReader.injectMethods().isEmpty() && hasNoConstructorParams) {
      writer.eol();
    }
  }

  private void writeMethodFields() {
    if (beanReader.injectMethods().isEmpty()) {
      return;
    }
    beanReader.injectMethods().stream()
      .flatMap(m -> m.params().stream())
      .filter(not(MethodParam::assisted))
      .forEach(p -> {
        var element = p.element();
        writer.append("  private %s %s$method;", UType.parse(element.asType()).shortType(), p.simpleName()).eol();
      });
    if (hasNoConstructorParams) {
      writer.eol();
    }
  }

  private void writeConstructor() {
    List<MethodParam> injectParams = beanReader.constructor().params().stream()
      .filter(not(MethodParam::assisted))
      .collect(Collectors.toList());

    if (injectParams.isEmpty()) {
      return;
    }

    writeFieldsForInjected(injectParams);

    String shortName = this.shortName;
    if (beanReader.beanType().getNestingKind().isNested()) {
      shortName = shortName.replace(".", "$");
    }
    writer.eol().append("  ").append(shortName).append(suffix).append("(");

    for (var iterator = injectParams.iterator(); iterator.hasNext(); ) {
      var p = iterator.next();
      var element = p.element();
      AnnotationCopier.copyAnnotations(writer, element, false);
      var type = UType.parse(element.asType());
      writer.append("%s %s", type.shortType(), p.simpleName());
      if (iterator.hasNext()) {
        writer.append(", ");
      }
    }

    writer.append(") {").eol();
    for (var p : injectParams) {
      writer.append("    ").append("this.%s = %s;", p.simpleName(), p.simpleName()).eol();
    }
    writer.append("  }").eol().eol();
  }

  private void writeFieldsForInjected(List<MethodParam> injectParams) {
    for (MethodParam p : injectParams) {
      var element = p.element();
      var type = UType.parse(element.asType()).shortType();
      writer.append("  private final %s %s;", type, p.simpleName()).eol();
    }
  }

  private void writeFactoryMethod() {
    writer.append(CODE_COMMENT_BUILD, shortName).eol();
    if (beanReader.hasTargetFactory()) {
      writer.append("  @Override").eol();
    }
    writer.append("  public %s %s(", shortName, beanReader.factoryMethodName());
    List<? extends VariableElement> params = beanReader.factoryMethodParams();
    for (var iterator = params.iterator(); iterator.hasNext(); ) {
      var element = iterator.next();
      var type = UType.parse(element.asType());
      writer.append("%s %s", type.shortWithoutAnnotations(), element.getSimpleName());
      if (iterator.hasNext()) {
        writer.append(", ");
      }
    }

    writer.append(") {").eol();

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
    String indent = "   ";
    writer.indent(indent).append(" var bean = new %s(", shortName);
    // add constructor dependencies
    writeMethodParams(constructor, true);
  }

  private void writeExtraInjection() {
    injectFields();
    injectMethods();
    writer.indent("    return bean;");
  }

  private void injectFields() {
    if (beanReader.injectFields().isEmpty()) {
      return;
    }
    for (FieldReader fieldReader : beanReader.injectFields()) {
      if (fieldReader.assisted()) {
        continue;
      }
      String fieldName = fieldReader.fieldName();
      String getDependency = fieldName + "$field";
      writer.indent("    ").append("bean.%s = %s;", fieldName, getDependency).eol();
    }

    assistedElements.stream()
      .filter(e -> e.getKind() == ElementKind.FIELD)
      .forEach(field ->
        writer
          .indent("    ")
          .append("bean.%s = %s;", field.getSimpleName(), field.getSimpleName())
          .eol());
  }

  private void injectMethods() {
    final var needsTry = beanReader.needsTryForMethodInjection();
    if (needsTry) {
      writer.indent("    try {").eol();
    }
    final var indent = needsTry ? "      " : "    ";
    for (MethodReader methodReader : beanReader.injectMethods()) {
      writer.indent(indent).append("bean.%s(", methodReader.name());
      writeMethodParams(methodReader, false);
    }
    if (needsTry) {
      writer.indent("    } catch (Throwable e) {").eol();
      writer.indent("      throw new RuntimeException(\"Error wiring method\", e);").eol();
      writer.indent("    }").eol();
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
        writer.append("%s", methodParam.simpleName());
      }
    }
    writer.append(");").eol();
  }

  private void writeClassEnd() {
    writer.append("}").eol();
  }

  private void writeInjectionMethods(MethodReader reader) {
    writer.eol();

    ExecutableElement methodElement = reader.element();
    AnnotationCopier.copyAnnotations(writer, methodElement, "  ", true);

    String simpleName = reader.name();
    String returnType = UType.parse(methodElement.getReturnType()).shortType();
    writer.append("  ").append(returnType).append(" ").append(simpleName).append("(");

    for (var iterator = reader.params().iterator(); iterator.hasNext(); ) {
      var p = iterator.next();
      if (p.assisted()) {
        continue;
      }
      var element = p.element();
      AnnotationCopier.copyAnnotations(writer, element, false);
      var type = UType.parse(element.asType());
      writer.append("%s %s", type.shortType(), p.simpleName());
      if (iterator.hasNext()) {
        writer.append(", ");
      }
    }

    writer.append(") {").eol();
    for (var p : reader.params()) {
      if (p.assisted()) {
        continue;
      }
      writer.append("    this.%s$method = %s;", p.simpleName(), p.simpleName()).eol();
    }
    writer.append("  }").eol();
  }
}
