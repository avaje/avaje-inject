package io.avaje.inject.generator;

import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;

/**
 * Write the source code for the bean.
 */
class SimpleBeanWriter {

  private static final String CODE_COMMENT = "/**\n * Generated source - dependency injection builder for %s.\n */";
  private static final String CODE_COMMENT_FACTORY = "/**\n * Generated source - dependency injection factory for request scoped %s.\n */";
  private static final String CODE_COMMENT_BUILD = "  /**\n   * Create and register %s.\n   */";

  private final BeanReader beanReader;
  private final ProcessingContext context;
  private final String originName;
  private final String shortName;
  private final String packageName;
  private final String suffix;
  private Append writer;

  SimpleBeanWriter(BeanReader beanReader, ProcessingContext context) {
    this.beanReader = beanReader;
    this.context = context;
    TypeElement origin = beanReader.getBeanType();
    this.originName = origin.getQualifiedName().toString();
    this.shortName = origin.getSimpleName().toString();
    this.packageName = Util.packageOf(originName);
    this.suffix = beanReader.suffix();
  }

  private Writer createFileWriter() throws IOException {
    JavaFileObject jfo = context.createWriter(originName + suffix);
    return jfo.openWriter();
  }

  void write() throws IOException {
    writer = new Append(createFileWriter());
    writePackage();
    writeImports();
    writeClassStart();
    if (isRequestScopedController()) {
      writeRequestCreate();
    } else {
      writeGenericTypeFields();
      writeGenericProviders();
      writeStaticFactoryMethod();
      writeStaticFactoryBeanMethods();
    }
    writeClassEnd();
    writer.close();
  }

  private void writeGenericProviders() {
    final Set<GenericType> genericTypes = beanReader.getGenericTypes();
    if (genericTypes != null && !genericTypes.isEmpty()) {
      for (GenericType type : genericTypes) {
        final String sn = type.shortName();
        writer.append("  public static Provider<");
        type.writeShort(writer);
        writer.append("> provider%s(Builder builder) {",  sn).eol();
        writer.append("    return builder.getProviderFor(%s.class, TYPE_%s);", shortName, sn).eol();
        writer.append("  }").eol();
      }
      writer.eol();
    }
  }

  private void writeGenericTypeFields() {
    final Set<GenericType> genericTypes = beanReader.getGenericTypes();
    if (genericTypes != null && !genericTypes.isEmpty()) {
      for (GenericType type : genericTypes) {
        writer.append("  public static final Type TYPE_%s = new GenericType<", type.shortName());
        type.writeShort(writer);
        writer.append(">(){};").eol();
      }
      writer.eol();
    }
  }

  private void writeRequestCreate() {
    beanReader.writeRequestCreate(writer);
  }

  private boolean isRequestScopedController() {
    return beanReader.isRequestScopedController();
  }

  private void writeStaticFactoryBeanMethods() {
    for (MethodReader factoryMethod : beanReader.getFactoryMethods()) {
      writeFactoryBeanMethod(factoryMethod);
    }
  }

  private void writeFactoryBeanMethod(MethodReader method) {
    method.commentBuildMethod(writer);
    writer.append("  public static void build_%s(Builder builder) {", method.getName()).eol();
    method.buildAddFor(writer);
    writer.append(method.builderGetFactory()).eol();
    writer.append(method.builderBuildBean()).eol();
    method.builderBuildAddBean(writer);
    writer.append("    }").eol();
    writer.append("  }").eol().eol();
  }

  private void writeStaticFactoryMethod() {
    MethodReader constructor = beanReader.getConstructor();
    if (constructor == null) {
      context.logError(beanReader.getBeanType(), "Unable to determine constructor to use for %s? Add explicit @Inject to one of the constructors.", beanReader.getBeanType());
      return;
    }
    writeBuildMethodStart(constructor);
    writeAddFor(constructor);
    writer.append("  }").eol().eol();
  }

  private void writeAddFor(MethodReader constructor) {
    beanReader.buildAddFor(writer);
    writeCreateBean(constructor);
    beanReader.buildRegister(writer);
    beanReader.addLifecycleCallbacks(writer);
    if (beanReader.isExtraInjectionRequired()) {
      writeExtraInjection();
    }
    writer.append("    }").eol();
  }

  private void writeBuildMethodStart(MethodReader constructor) {
    int providerIndex = 0;
    writer.append(CODE_COMMENT_BUILD, shortName).eol();
    writer.append("  public static void build(Builder builder");
    for (MethodReader.MethodParam param : constructor.getParams()) {
      if (param.isGenericParam()) {
        param.addProviderParam(writer, providerIndex++);
      }
    }
    for (MethodReader methodReader : beanReader.getInjectMethods()) {
      for (MethodReader.MethodParam param : methodReader.getParams()) {
        if (param.isGenericParam()) {
          param.addProviderParam(writer, providerIndex++);
        }
      }
    }
    writer.append(") {").eol();
  }

  private void writeCreateBean(MethodReader constructor) {
    writer.append("      %s bean = new %s(", shortName, shortName);
    // add constructor dependencies
    writeMethodParams("builder", constructor);
  }

  private void writeExtraInjection() {
    writer.append("      builder.addInjector(b -> {").eol();
    writer.append("        // field and method injection").eol();
    injectFields();
    injectMethods();
    writer.append("      });").eol();
  }

  private void injectFields() {
    for (FieldReader fieldReader : beanReader.getInjectFields()) {
      String fieldName = fieldReader.getFieldName();
      String getDependency = fieldReader.builderGetDependency();
      writer.append("        $bean.%s = %s;", fieldName, getDependency).eol();
    }
  }

  private void injectMethods() {
    for (MethodReader methodReader : beanReader.getInjectMethods()) {
      writer.append("        $bean.%s(", methodReader.getName());
      writeMethodParams("b", methodReader);
    }
  }

  private void writeMethodParams(String builderRef, MethodReader methodReader) {
    List<MethodReader.MethodParam> methodParams = methodReader.getParams();
    for (int i = 0; i < methodParams.size(); i++) {
      if (i > 0) {
        writer.append(", ");
      }
      writer.append(methodParams.get(i).builderGetDependency(builderRef));
    }
    writer.append(");").eol();
  }

  private void writeImports() {
    beanReader.writeImports(writer);
  }

  private void writeClassEnd() {
    writer.append("}").eol();
  }

  private void writeClassStart() {
    if (beanReader.isRequestScopedController()) {
      writer.append(CODE_COMMENT_FACTORY, shortName).eol();
    } else {
      writer.append(CODE_COMMENT, shortName).eol();
    }
    writer.append(Constants.AT_GENERATED).eol();
    if (beanReader.isRequestScopedController()) {
      writer.append(Constants.AT_SINGLETON).eol();
    }
    writer.append("public class ").append(shortName).append(suffix).append(" ");
    if (beanReader.isRequestScopedController()) {
      writer.append("implements ");
      beanReader.factoryInterface(writer);
    }
    writer.append(" {").eol().eol();
  }

  private void writePackage() {
    if (packageName != null) {
      writer.append("package %s;", packageName).eol().eol();
    }
  }
}
