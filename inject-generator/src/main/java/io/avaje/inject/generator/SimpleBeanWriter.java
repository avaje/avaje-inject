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
    if (isRequestScoped()) {
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

  private boolean isRequestScoped() {
    return beanReader.isRequestScoped();
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
      context.logError(beanReader.getBeanType(), "Unable to determine constructor to use?");
      return;
    }

    int providerIndex = 0;
    writer.append("  public static void build(Builder builder");
    for (MethodReader.MethodParam param : constructor.getParams()) {
      if (param.isGenericType()) {
        param.addProviderParam(writer, providerIndex++);
      }
    }
    for (MethodReader methodReader : beanReader.getInjectMethods()) {
      for (MethodReader.MethodParam param : methodReader.getParams()) {
        if (param.isGenericType()) {
          param.addProviderParam(writer, providerIndex++);
        }
      }
    }
    writer.append(") {").eol();

    beanReader.buildAddFor(writer);
    writer.append("      %s bean = new %s(", shortName, shortName);

    // add constructor dependencies
    List<MethodReader.MethodParam> params = constructor.getParams();
    for (int i = 0; i < params.size(); i++) {
      if (i > 0) {
        writer.append(",");
      }
      writer.append(params.get(i).builderGetDependency("builder"));
    }
    writer.append(");").eol();

    beanReader.buildRegister(writer);
    if (beanReader.isLifecycleRequired()) {
      beanReader.buildAddLifecycle(writer);
    }
    if (beanReader.isFieldInjectionRequired() || beanReader.isMethodInjectionRequired()) {
      writer.append("      builder.addInjector(b -> {").eol();
      for (FieldReader fieldReader : beanReader.getInjectFields()) {
        String fieldName = fieldReader.getFieldName();
        String getDependency = fieldReader.builderGetDependency();
        writer.append("        $bean.%s = %s;", fieldName, getDependency).eol();
      }
      for (MethodReader methodReader : beanReader.getInjectMethods()) {
        writer.append("        $bean.%s(", methodReader.getName());
        List<MethodReader.MethodParam> methodParams = methodReader.getParams();
        for (int i = 0; i < methodParams.size(); i++) {
          if (i > 0) {
            writer.append(",");
          }
          writer.append(methodParams.get(i).builderGetDependency("b"));
        }
        writer.append(");").eol();
      }
      writer.append("      });").eol();
    }
    writer.append("    }").eol();
    writer.append("  }").eol().eol();
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
    if (context.isGeneratedAvailable()) {
      writer.append(Constants.AT_GENERATED).eol();
    }
    if (beanReader.isRequestScoped()) {
      writer.append(Constants.AT_SINGLETON).eol();
    }
    writer.append("public class ").append(shortName).append(suffix).append(" ");
    if (beanReader.isLifecycleWrapperRequired()) {
      writer.append("implements BeanLifecycle ");
    }
    if (beanReader.isRequestScoped()) {
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
