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

  AspectMethod(ProcessingContext context, AspectPair aspect, ExecutableElement method) {
    this.aspect = aspect;
    this.method = method;
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
    //MethodReader mr = new MethodReader(method);
    String simpleName = method.getSimpleName().toString();
    writer.eol().append("  @Override").eol();
    writer.append("  public %s %s(", rawReturn, simpleName);
    for (int i = 0, size = params.size(); i < size; i++) {
      if (i > 0) {
        writer.append(", ");
      }
      params.get(i).writeMethodParam(writer);
    }
    writer.append(") {").eol();
    aspectTarget.writeBefore(writer);


    writer.append("    ");
    invokeSuper(writer, simpleName);
    writer.append("  }").eol();
  }

  private void invokeSuper(Append writer, String simpleName) {
    if (!isVoid()) {
      writer.append("return ");
    }
    writer.append("super.%s(", simpleName);
    for (int i = 0, size = params.size(); i < size; i++) {
      if (i > 0) {
        writer.append(", ");
      }
      writer.append(params.get(i).simpleName());
    }
    writer.append(");").eol();
  }

}
