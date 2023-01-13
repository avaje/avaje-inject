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
final class SimpleBeanWriter {

  private static final String CODE_COMMENT = "/**\n * Generated source - dependency injection builder for %s.\n */";
  private static final String CODE_COMMENT_FACTORY = "/**\n * Generated source - dependency injection factory for request scoped %s.\n */";
  private static final String CODE_COMMENT_BUILD = "  /**\n   * Create and register %s.\n   */";
  private static final String CODE_COMMENT_BUILD_PROVIDER = "  /**\n   * Register %s provider.\n   */";

  private final BeanReader beanReader;
  private final ProcessingContext context;
  private final String originName;
  private final String shortName;
  private final String packageName;
  private final String suffix;
  private final boolean proxied;
  private Append writer;

  SimpleBeanWriter(BeanReader beanReader, ProcessingContext context) {
    this.beanReader = beanReader;
    this.context = context;
    TypeElement origin = beanReader.beanType();
    this.originName = origin.getQualifiedName().toString();
    if (origin.getNestingKind().isNested()) {
      this.packageName = Util.nestedPackageOf(originName);
      this.shortName = Util.nestedShortName(originName);
    } else {
      this.packageName = Util.packageOf(originName);
      this.shortName = Util.shortName(originName);
    }
    this.suffix = beanReader.suffix();
    this.proxied = beanReader.isGenerateProxy();
  }

  private Writer createFileWriter() throws IOException {
    String originName = this.originName;
    if (beanReader.beanType().getNestingKind().isNested()) {
      originName = originName.replace(shortName, shortName.replace(".", "$"));
    }
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
      writeStaticFactoryMethod();
      writeStaticFactoryBeanMethods();
    }
    writeClassEnd();
    writer.close();
  }

  private void writeGenericTypeFields() {
    // collect all types to prevent duplicates
    Set<GenericType> genericTypes = beanReader.allGenericTypes();
    if (!genericTypes.isEmpty()) {
      for (GenericType type : genericTypes) {
        writer.append("  public static final Type TYPE_%s = new GenericType<", type.shortName());
        // use fully qualified types here rather than use type.writeShort(writer)
        writer.append(type.toString());
        writer.append(">(){}.type();").eol();
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
    for (MethodReader factoryMethod : beanReader.factoryMethods()) {
      writeFactoryBeanMethod(factoryMethod);
    }
  }

  private void writeFactoryBeanMethod(MethodReader method) {
    method.commentBuildMethod(writer);
    writer.append("  public static void build_%s(%s builder) {", method.name(), beanReader.builderType()).eol();
    method.buildAddFor(writer);
    writer.append(method.builderGetFactory()).eol();
    if (method.isProtoType()) {
      method.builderAddBeanProvider(writer);
    } else if (method.isUseProviderForSecondary()) {
      method.builderAddBeanProvider(writer);
    } else {
      method.builderBuildBean(writer);
      method.builderBuildAddBean(writer);
      writer.append("    }").eol();
    }
    writer.append("  }").eol().eol();
  }

  private void writeStaticFactoryMethod() {
    MethodReader constructor = beanReader.constructor();
    if (constructor == null) {
      context.logError(beanReader.beanType(), "Unable to determine constructor to use for %s? Add explicit @Inject to one of the constructors.", beanReader.beanType());
      return;
    }
    writeBuildMethodStart();
    if (proxied) {
      writer.append("    // this bean is proxied, see %s$Proxy$DI instead", shortName).eol();
    } else {
      writeAddFor(constructor);
    }
    writer.append("  }").eol().eol();
  }

  private void writeAddFor(MethodReader constructor) {
    beanReader.buildAddFor(writer);
    if (beanReader.prototype()) {
      indent += "  ";
      writer.append("      builder.asPrototype().registerProvider(() -> {", shortName, shortName).eol();
    }
    writeCreateBean(constructor);
    beanReader.buildRegister(writer);
    beanReader.addLifecycleCallbacks(writer, indent);
    if (beanReader.isExtraInjectionRequired()) {
      writeExtraInjection();
    }
    if (beanReader.prototype()) {
      beanReader.prototypePostConstruct(writer, indent);
      writer.append("        return bean;").eol();
      writer.append("      });", shortName, shortName).eol();
    }
    writer.append("    }").eol();
  }

  private void writeBuildMethodStart() {
    if (beanReader.prototype()) {
      writer.append(CODE_COMMENT_BUILD_PROVIDER, shortName).eol();
    } else {
      writer.append(CODE_COMMENT_BUILD, shortName).eol();
    }
    writer.append("  public static void build(%s builder) {", beanReader.builderType()).eol();
  }

  String indent = "     ";
  private void writeCreateBean(MethodReader constructor) {
    writer.append(indent).append(" var bean = new %s(", shortName);
    // add constructor dependencies
    writeMethodParams("builder", constructor);
  }

  private void writeExtraInjection() {
    if (!beanReader.prototype()) {
      writer.append("      builder.addInjector(b -> {").eol();
      writer.append("        // field and method injection").eol();
    }
    injectFields();
    injectMethods();
    if (!beanReader.prototype()) {
      writer.append("      });").eol();
    }
  }

  private void injectFields() {
    String bean = beanReader.prototype() ? "bean" : "$bean";
    String builder = beanReader.prototype() ? "builder" : "b";
    for (FieldReader fieldReader : beanReader.injectFields()) {
      String fieldName = fieldReader.fieldName();
      String getDependency = fieldReader.builderGetDependency(builder);
      writer.append("        %s.%s = %s;", bean, fieldName, getDependency).eol();
    }
  }

  private void injectMethods() {
    String bean = beanReader.prototype() ? "bean" : "$bean";
    String builder = beanReader.prototype() ? "builder" : "b";
    for (MethodReader methodReader : beanReader.injectMethods()) {
      writer.append("        %s.%s(", bean, methodReader.name());
      writeMethodParams(builder, methodReader);
    }
  }

  private void writeMethodParams(String builderRef, MethodReader methodReader) {
    List<MethodReader.MethodParam> methodParams = methodReader.params();
    for (int i = 0; i < methodParams.size(); i++) {
      if (i > 0) {
        writer.append(", ");
      }
      methodParams.get(i).builderGetDependency(writer, builderRef, false);
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
    writer.append(beanReader.generatedType()).append(Constants.AT_GENERATED_COMMENT).eol();
    if (beanReader.isRequestScopedController()) {
      writer.append(Constants.AT_SINGLETON).eol();
    }
    String shortName = this.shortName;
    if (beanReader.beanType().getNestingKind().isNested()) {
      shortName = shortName.replace(".", "$");
    }
    writer.append("public final class ").append(shortName).append(suffix).append(" ");
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
