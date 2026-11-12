package io.avaje.inject.generator;

import static io.avaje.inject.generator.APContext.createSourceFile;

import java.io.IOException;
import java.io.Writer;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.tools.JavaFileObject;

final class SimpleInjectorWriter {

  private static final String CODE_COMMENT =
      "/**\n * Generated source that injects dependencies into an externally created %s.\n */";
  private static final String TRACKING_FIELD = "injected$targets";
  private final InjectorBeanReader beanReader;
  private final String originName;
  private final String shortName;
  private final String packageName;
  private final String suffix;
  private Append writer;

  SimpleInjectorWriter(InjectorBeanReader beanReader) {
    this.beanReader = beanReader;
    this.packageName = beanReader.packageName();
    this.shortName = beanReader.shortName();
    this.suffix = "$Injector";
    this.originName = packageName.isBlank() ? shortName : packageName + "." + shortName;
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
    writeTrackingField();
    writeInjectFields();
    writeMethodFields();
    writeInjectMethod();
    writePreDestroyAllMethod();
    beanReader.injectMethods().forEach(this::writeInjectionMethod);
    writeClassEnd();
    writer.close();
  }

  private boolean hasPreDestroy() {
    return beanReader.preDestroyMethod() != null;
  }

  private void writePackage() {
    if (packageName != null && !packageName.isBlank()) {
      writer.append("package %s;", packageName).eol().eol();
    }
  }

  private void writeImports() {
    beanReader.writeImports(writer, packageName);
  }

  private String targetShortName() {
    String name = shortName;
    if (beanReader.beanType().getNestingKind().isNested()) {
      name = name.replace(".", "$");
    }
    return name;
  }

  private void writeClassStart() {
    writer.append(CODE_COMMENT, shortName).eol();
    writer.append(Constants.AT_SUPPRESS_WARNINGS).eol();
    writer.append(Constants.AT_GENERATED).eol();
    writer.append("@io.avaje.inject.Component").eol();
    writer
        .append(
            "public final class %s%s implements Injector<%s> {",
            targetShortName(), suffix, targetShortName())
        .eol()
        .eol();
  }

  private void writeTrackingField() {
    if (!hasPreDestroy()) {
      return;
    }
    writer.append("  private final Set<%s> %s =", targetShortName(), TRACKING_FIELD).eol();
    writer
        .append(
            "      Collections.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));")
        .eol()
        .eol();
  }

  private void writeInjectFields() {
    for (FieldReader field : beanReader.injectFields()) {
      var element = field.element();
      AnnotationCopier.copyAnnotations(writer, element, "  ", true);
      var type = UType.parse(element.asType());
      writer.append("  %s %s$field;", type.shortType(), field.fieldName()).eol().eol();
    }
  }

  private void writeMethodFields() {
    beanReader.injectMethods().stream()
        .flatMap(m -> m.params().stream())
        .forEach(
            p -> {
              var element = p.element();
              writer
                  .append(
                      "  private %s %s$method;",
                      UType.parse(element.asType()).shortType(), p.simpleName())
                  .eol();
            });
    if (!beanReader.injectMethods().isEmpty()) {
      writer.eol();
    }
  }

  private void writeInjectMethod() {
    writer.append("  /**").eol();
    writer
        .append("   * Inject the dependencies into the given %s instance.", targetShortName())
        .eol();
    writer.append("   */").eol();
    writer.append("  @Override").eol();
    writer.append("  public void inject(%s bean) {", targetShortName()).eol();
    final var needsTry = beanReader.needsTryForMethodInjection();
    if (needsTry) {
      writer.start("try {").eol().incIndent();
    }
    for (FieldReader fieldReader : beanReader.injectFields()) {
      var name = fieldReader.fieldName();
      writer.start("bean.%s = %s$field;", name, name).eol();
    }
    for (MethodReader methodReader : beanReader.injectMethods()) {
      writer.start("bean.%s(", methodReader.name());
      var params = methodReader.params();
      for (int i = 0; i < params.size(); i++) {
        if (i > 0) {
          writer.append(", ");
        }
        writer.append("%s$method", params.get(i).simpleName());
      }
      writer.append(");").eol();
    }
    if (hasPreDestroy()) {
      writer.start("%s.add(bean);", TRACKING_FIELD).eol();
    }
    if (needsTry) {
      writer.decIndent();
      writer.start("} catch (Throwable e) {").eol();
      writer.start("  throw new RuntimeException(\"Error wiring bean\", e);").eol();
      writer.start("}").eol();
    }
    writer.append("  }").eol();
  }

  private void writePreDestroyAllMethod() {
    Element preDestroy = beanReader.preDestroyMethod();
    if (preDestroy == null) {
      return;
    }
    var name = targetShortName();
    var methodName = preDestroy.getSimpleName().toString();
    var methodThrows = !((ExecutableElement) preDestroy).getThrownTypes().isEmpty();
    writer.eol();
    writer.append("  /**").eol();
    writer
        .append(
            "   * Invoke {@code @PreDestroy} on every %s instance this injector injected.", name)
        .eol();
    writer.append("   */").eol();
    writer.append("  @PreDestroy").eol();
    writer.append("  void preDestroyAll$() {").eol();
    writer.start("for (%s bean : %s) {", name, TRACKING_FIELD).eol().incIndent();
    if (methodThrows) {
      writer.start("try {").eol().incIndent();
      writer.start("bean.%s();", methodName).eol();
      writer.decIndent();
      writer.start("} catch (Throwable e) {").eol().incIndent();
      writer.start("throw new RuntimeException(\"Error calling @PreDestroy method\", e);").eol();
      writer.decIndent();
      writer.start("}").eol();
    } else {
      writer.start("bean.%s();", methodName).eol();
    }
    writer.decIndent();
    writer.start("}").eol();
    writer.start("%s.clear();", TRACKING_FIELD).eol();
    writer.append("  }").eol();
  }

  private void writeInjectionMethod(MethodReader reader) {
    writer.eol();
    ExecutableElement methodElement = reader.element();
    AnnotationCopier.copyAnnotations(writer, methodElement, "  ", true);
    writer.append("  void %s(", reader.name());
    for (var iterator = reader.params().iterator(); iterator.hasNext(); ) {
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
    for (var p : reader.params()) {
      writer.append("    this.%s$method = %s;", p.simpleName(), p.simpleName()).eol();
    }
    writer.append("  }").eol();
  }

  private void writeClassEnd() {
    writer.append("}").eol();
  }
}
