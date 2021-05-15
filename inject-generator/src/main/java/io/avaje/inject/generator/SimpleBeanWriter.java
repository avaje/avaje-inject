package io.avaje.inject.generator;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Write the source code for the bean.
 */
class SimpleBeanWriter {

  private static final String CODE_COMMENT = "/**\n * Generated source - dependency injection builder for %s.\n */";
  private static final String CODE_COMMENT_FACTORY = "/**\n * Generated source - dependency injection factory for request scoped %s.\n */";
  private static final String CODE_COMMENT_LIFECYCLE = "  /**\n   * Lifecycle wrapper for %s.\n   */";
  private static final String CODE_COMMENT_BUILD = "  /**\n   * Create and register %s.\n   */";
  private static final String CODE_COMMENT_BUILD_RSB = "  /**\n   * Register provider for request scoped %s.\n   */";

  private final BeanReader beanReader;
  private final ProcessingContext context;
  private final String originName;
  private final String shortName;
  private final String packageName;
  private final String suffix;
  private Append writer;
  private String indent = "     ";

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
      writeStaticFactoryMethod();
      writeStaticFactoryBeanMethods();
      writeLifecycleWrapper();
      writeStaticFactoryBeanLifecycle();
    }
    writeClassEnd();
    writer.close();
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

  private void writeStaticFactoryBeanLifecycle() {
    for (MethodReader factoryMethod : beanReader.getFactoryMethods()) {
      factoryMethod.buildLifecycleClass(writer);
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
    if (beanReader.isRequestScopedBean()) {
      writeReqScopeBean(constructor);
    } else {
      writeAddFor(constructor);
    }
    writer.append("  }").eol().eol();
  }

  private void writeReqScopeBean(MethodReader constructor) {
    indent = indent + "  ";
    beanReader.buildReq(writer);
    writeCreateBean(constructor, "scope");
    if (beanReader.isExtraInjectionRequired()) {
      writeExtraInjection();
    }
    beanReader.writePostConstruct(writer);
    beanReader.writePreDestroy(writer);
    beanReader.buildReqEnd(writer);
  }

  private void writeAddFor(MethodReader constructor) {
    beanReader.buildAddFor(writer);
    writeCreateBean(constructor, "builder");
    beanReader.buildRegister(writer);
    if (beanReader.isLifecycleRequired()) {
      beanReader.buildAddLifecycle(writer);
    }
    if (beanReader.isExtraInjectionRequired()) {
      writeExtraInjection();
    }
    writer.append("    }").eol();
  }

  private void writeBuildMethodStart(MethodReader constructor) {
    int providerIndex = 0;
    if (beanReader.isRequestScopedBean()) {
      writer.append(CODE_COMMENT_BUILD_RSB, shortName).eol();
    } else {
      writer.append(CODE_COMMENT_BUILD, shortName).eol();
    }
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

  private void writeCreateBean(MethodReader constructor, String builderName) {
    writer.append("%s %s bean = new %s(", indent, shortName, shortName);
    // add constructor dependencies
    writeMethodParams(builderName, constructor);
  }

  private void writeExtraInjection() {
    String builderRef = "b";
    String beanRef = "$bean";
    if (beanReader.isRequestScopedBean()) {
      builderRef = "scope";
      beanRef = "bean";
    } else {
      writer.append("      builder.addInjector(b -> {").eol();
    }
    writer.append("        // field and method injection").eol();
    for (FieldReader fieldReader : beanReader.getInjectFields()) {
      String fieldName = fieldReader.getFieldName();
      String getDependency = fieldReader.builderGetDependency(builderRef);
      writer.append("        %s.%s = %s;", beanRef, fieldName, getDependency).eol();
    }
    for (MethodReader methodReader : beanReader.getInjectMethods()) {
      writer.append("        %s.%s(", beanRef, methodReader.getName());
      writeMethodParams(builderRef, methodReader);
    }
    if (!beanReader.isRequestScopedBean()) {
      writer.append("      });").eol();
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

  private void lifecycleMethod(String method, Element methodElement) {
    writer.append("  @Override").eol();
    writer.append("  public void %s() {", method).eol();
    if (methodElement == null) {
      writer.append("    // do nothing for %s", method).eol();
    } else {
      String methodName = methodElement.getSimpleName().toString();
      writer.append("    bean.%s();", methodName).eol();
    }
    writer.append("  }").eol().eol();
  }

  private void writeLifecycleWrapper() {
    if (beanReader.isLifecycleWrapperRequired()) {
      writer.append("  private final %s bean;", shortName).eol().eol();
      writer.append(CODE_COMMENT_LIFECYCLE, shortName).eol();
      writer.append("  public %s%s(%s bean) {", shortName, suffix, shortName).eol();
      writer.append("    this.bean = bean;").eol();
      writer.append("  }").eol().eol();

      lifecycleMethod("postConstruct", beanReader.getPostConstructMethod());
      lifecycleMethod("preDestroy", beanReader.getPreDestroyMethod());
    }
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
    if (beanReader.isLifecycleWrapperRequired()) {
      writer.append("implements BeanLifecycle ");
    }
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
