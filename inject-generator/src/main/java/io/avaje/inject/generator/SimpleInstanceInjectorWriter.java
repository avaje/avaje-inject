package io.avaje.inject.generator;

import static io.avaje.inject.generator.APContext.createSourceFile;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

import io.avaje.inject.generator.MethodReader.MethodParam;

final class SimpleInstanceInjectorWriter {

  private final InstanceInjectorReader reader;
  private Append writer;

  SimpleInstanceInjectorWriter(InstanceInjectorReader reader) {
    this.reader = reader;
  }

  private Writer createFileWriter() throws IOException {
    final JavaFileObject file = createSourceFile(reader.generatedTypeName());
    return file.openWriter();
  }

  void write() throws IOException {
    writer = new Append(createFileWriter());
    writePackage();
    writeImports();
    writeClass();
    GenericTypeFieldsWriter.write(writer, reader.genericTypes());
    writeRegistration();
    writeInject();
    writePreDestroy();
    writer.append("}").eol();
    writer.close();
  }

  private void writePackage() {
    if (!reader.packageName().isBlank()) {
      writer.append("package %s;", reader.packageName()).eol().eol();
    }
  }

  private void writeImports() {
    for (String importType : reader.imports()) {
      if (Util.validImportType(importType, reader.packageName())) {
        writer.append("import %s;", Util.sanitizeImports(importType)).eol();
      }
    }
    writer.eol();
  }

  private void writeClass() {
    writer.append("@Generated(\"io.avaje.inject.generator\")").eol();
    writer.append("public final class %s implements", reader.generatedName()).eol();
    writer.append("    InstanceInjector<%s> {", reader.targetName()).eol();
  }

  private void writeRegistration() {
    writer.eol();
    writer.append("  public static void register(Builder builder) {").eol();
    writer.append("    builder.addInstanceInjector(%s.class, new %s());",
        reader.targetName(), reader.generatedName()).eol();
    writer.append("  }").eol();
  }

  private void writeInject() {
    writer.eol();
    writer.append("  @Override").eol();
    writer.append("  public void inject(%s instance, BeanScope scope) {",
        reader.targetName()).eol();

    final boolean needsTry =
        reader.injectMethods().stream().anyMatch(MethodReader::methodThrows)
            || reader.postConstructMethod().map(MethodReader::methodThrows).orElse(false);
    if (needsTry) {
      writer.start("try {").eol().incIndent();
    }

    for (FieldReader field : reader.injectFields()) {
      writeFieldInjection(field);
    }
    for (MethodReader method : reader.injectMethods()) {
      writeMethodInjection(method);
    }
    reader.postConstructMethod().ifPresent(this::writeMethodInjection);

    if (needsTry) {
      writer.decIndent();
      writer.start("} catch (Throwable e) {").eol();
      writer.start("  throw new RuntimeException(\"Error injecting instance\", e);").eol();
      writer.start("}").eol();
    }
    writer.append("  }").eol();
  }

  private void writeFieldInjection(FieldReader field) {
    final String value =
        isBeanScope(field.type().fullWithoutAnnotations())
        ? "scope"
        : field.scopeGetDependency("scope");
    writer.start("%s.%s = %s;", fieldOwner(field), field.fieldName(), value).eol();
  }

  private String fieldOwner(FieldReader field) {
    final var declaringType = (TypeElement) field.element().getEnclosingElement();
    if (declaringType.equals(reader.targetType())) {
      return "instance";
    }
    return String.format("((%s) instance)", declaringType.getQualifiedName());
  }

  private void writeMethodInjection(MethodReader method) {
    writer.start("instance.%s(", method.name());
    writeMethodParams(method.params());
    writer.append(");").eol();
  }

  private void writeMethodParams(List<MethodParam> params) {
    for (int i = 0; i < params.size(); i++) {
      if (i > 0) {
        writer.append(", ");
      }
      final MethodParam param = params.get(i);
      if (isBeanScope(param.getFullUType().fullWithoutAnnotations())) {
        writer.append("scope");
      } else {
        param.scopeGetDependency(writer, "scope");
      }
    }
  }

  private boolean isBeanScope(String type) {
    return Constants.BEANSCOPE.equals(type);
  }

  private void writePreDestroy() {
    if (!reader.hasPreDestroy()) {
      return;
    }
    writer.eol();
    writer.append("  @Override").eol();
    writer.append("  public boolean hasPreDestroy() {").eol();
    writer.append("    return true;").eol();
    writer.append("  }").eol();

    final Integer priority = reader.preDestroyPriority();
    if (priority != null) {
      writer.eol();
      writer.append("  @Override").eol();
      writer.append("  public int preDestroyPriority() {").eol();
      writer.append("    return %d;", priority).eol();
      writer.append("  }").eol();
    }

    writer.eol();
    writer.append("  @Override").eol();
    writer.append("  public void preDestroy(%s instance) {", reader.targetName()).eol();
    writer.start("try {").eol().incIndent();
    if (reader.preDestroyMethod() != null) {
      writer.start("instance.%s();", reader.preDestroyMethod().getSimpleName()).eol();
    } else {
      writer.start("((AutoCloseable) instance).close();").eol();
    }
    writer.decIndent();
    writer.start("} catch (Throwable e) {").eol();
    writer.start("  throw new RuntimeException(\"Error destroying injected instance\", e);").eol();
    writer.start("}").eol();
    writer.append("  }").eol();
  }
}
