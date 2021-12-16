package io.avaje.inject.generator;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class AspectMethod {
  private final AspectPair aspect;
  private final ExecutableElement method;
  private final List<MethodReader.MethodParam> params;
  private final TypeMirror returnMirror;
  private final String rawReturn;
  private final AspectTarget aspectTarget;
  private final String simpleName;

  AspectMethod(ProcessingContext context, AspectPair aspect, ExecutableElement method) {
    this.aspect = aspect;
    this.method = method;
    this.simpleName = method.getSimpleName().toString();
    this.params = initParams(method.getParameters());
    this.returnMirror = method.getReturnType();
    this.rawReturn = returnMirror.toString();
    this.aspectTarget = context.findAspectTarget(aspect.target());
  }

  List<MethodReader.MethodParam> initParams(List<? extends VariableElement> parameters) {
    List<MethodReader.MethodParam> mps = new ArrayList<>(parameters.size());
    for (VariableElement var : parameters) {
      mps.add(new MethodReader.MethodParam(var));
    }
    return mps;
  }

  String target() {
    return aspect.target();
  }

  boolean isVoid() {
    return rawReturn.equals("void");
  }

  void addImports(Set<String> importTypes) {
    aspect.addImports(importTypes);
  }

  void writeMethod(Append writer) {
    writer.eol().append("  @Override").eol();
    writer.append("  public %s %s(", rawReturn, simpleName);
    for (int i = 0, size = params.size(); i < size; i++) {
      if (i > 0) {
        writer.append(", ");
      }
      params.get(i).writeMethodParam(writer);
    }
    writer.append(") {").eol();
    aspectTarget.writeBefore(writer, this);
    invokeSuper(writer, simpleName);
    writer.append(")").eol();
    aspectTarget.writeAfter(writer, this);
    writer.append("  }").eol();
  }

  private void invokeSuper(Append writer, String simpleName) {
    writer.append(" super.%s(", simpleName);
    for (int i = 0, size = params.size(); i < size; i++) {
      if (i > 0) {
        writer.append(", ");
      }
      writer.append(params.get(i).simpleName());
    }
    writer.append(")");
  }

  void writeSetupFields(Append writer) {
    writer.append("  private Method %sMethod;", simpleName).eol();
    writer.append("  private MethodInterceptor %sInterceptor;", simpleName).eol();
  }

  void writeSetupForMethods(Append writer, String shortName) {
    writer.append("      %sMethod = %s.class.getDeclaredMethod(\"%s\"", simpleName, shortName, simpleName);
    for (MethodReader.MethodParam param : params) {
      writer.append(", ");
      param.writeMethodParamType(writer);
      writer.append(".class");
    }
    writer.append(");").eol();
  }

  void writeSetupForMethodsInterceptor(Append writer) {
    String target = aspect.target();
    String name = AspectTarget.shortName(target);
    String annoShortName = aspect.annotationShortName();
    writer.append("      %sInterceptor = %s.interceptor(%sMethod, %sMethod.getAnnotation(%s.class));", simpleName, name, simpleName, simpleName, annoShortName).eol();
  }

  void writeArgs(Append writer) {
    if (!params.isEmpty()) {
      writer.append("      .arguments(");
      for (int i = 0, size = params.size(); i < size; i++) {
        if (i > 0) {
          writer.append(", ");
        }
        writer.append(params.get(i).simpleName());
      }
      writer.append(")").eol();
    }
    writer.append("      .method(%sMethod);", simpleName).eol();
    writer.append("    try {").eol();
    writer.append("      %sInterceptor.invoke(call);", simpleName).eol();
    if (!isVoid()) {
      writer.append("      return call.finalResult();").eol();
    }
    writer.append("    } catch (Throwable e) {").eol();
    writer.append("      throw new RuntimeException(e);").eol();
    writer.append("    }").eol();
  }
}
