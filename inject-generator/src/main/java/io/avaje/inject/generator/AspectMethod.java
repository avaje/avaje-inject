package io.avaje.inject.generator;

import io.avaje.inject.generator.MethodReader.MethodParam;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.*;

final class AspectMethod {

  private final List<AspectPair> aspectPairs;
  private final ExecutableElement method;
  private final List<MethodReader.MethodParam> params;
  private final String rawReturn;
  private final String simpleName;
  private final List<? extends TypeMirror> thrownTypes;
  private final String localName;

  AspectMethod(int nameIndex, List<AspectPair> aspectPairs, ExecutableElement method) {
    this.aspectPairs = sort(aspectPairs);
    this.method = method;
    this.simpleName = method.getSimpleName().toString();
    this.params = initParams(method.getParameters());
    this.rawReturn = method.getReturnType().toString();
    this.thrownTypes = method.getThrownTypes();
    this.localName = simpleName + nameIndex;
  }

  private List<AspectPair> sort(List<AspectPair> aspectPairs) {
    Collections.sort(aspectPairs);
    return aspectPairs;
  }

  List<MethodReader.MethodParam> initParams(List<? extends VariableElement> parameters) {
    List<MethodReader.MethodParam> mps = new ArrayList<>(parameters.size());
    for (VariableElement var : parameters) {
      mps.add(new MethodReader.MethodParam(var));
    }
    return mps;
  }

  void addTargets(Set<String> targets) {
    for (AspectPair aspectPair : aspectPairs) {
      targets.add(aspectPair.annotationShortName());
    }
  }

  boolean isVoid() {
    return rawReturn.equals("void");
  }

  void addImports(ImportTypeMap importTypes) {
    for (AspectPair aspect : aspectPairs) {
      aspect.addImports(importTypes);
    }
    for (MethodParam param : params) {
      param.addImports(importTypes);
    }
    for (TypeMirror thrownType : method.getThrownTypes()) {
      importTypes.add(thrownType.toString());
    }
  }

  void writeMethod(Append writer) {
    writer.eol().append("  @Override").eol();
    writer.append("  public %s %s(", rawReturn, simpleName);
    for (int i = 0, size = params.size(); i < size; i++) {
      if (i > 0) {
        writer.append(", ");
      }
      params.get(i).writeMethodParamAspect(writer);
    }
    writer.append(")");
    writeThrowsClause(writer);

    writer.append(" {").eol();

    String type = isVoid() ? "Run" : "Call<>";
    writer.append("    var call = new Invocation.%s(() ->", type);
    invokeSuper(writer, simpleName);
    writer.append(")").eol();
    writeArgs(writer);
    writer.append("  }").eol();
  }

  private void writeThrowsClause(Append writer) {
    if (!thrownTypes.isEmpty()) {
      writer.append(" throws ");
      for (int i = 0; i < thrownTypes.size(); i++) {
        if (i > 0) {
          writer.append(", ");
        }
        writer.append(Util.shortName(thrownTypes.get(i).toString()));
      }
    }
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
    writer.append("  private final Method %s;", localName).eol();
    for (AspectPair aspectPair : aspectPairs) {
      String sn = aspectPair.annotationShortName();
      writer.append("  private final MethodInterceptor %s%s;", localName, sn).eol();
    }
  }

  void writeSetupForMethods(Append writer, String shortName) {
    writer.append("      %s = %s.class.getDeclaredMethod(\"%s\"", localName, shortName, simpleName);
    for (MethodReader.MethodParam param : params) {
      writer.append(", ");
      param.writeMethodParamTypeAspect(writer);
      writer.append(".class");
    }
    writer.append(");").eol();
    for (AspectPair aspect : aspectPairs) {
      String name = Util.initLower(aspect.annotationShortName());
      String sn = aspect.annotationShortName();
      writer.append("      %s%s = %s.interceptor(%s, %s.getAnnotation(%s.class));", localName, sn, name, localName, localName, sn).eol();
    }
    writer.eol();
  }

  static String aspectTargetShortName(String target) {
    String type = Util.shortName(target);
    return Util.initLower(type);
  }

  void writeArgs(Append writer) {
    writer.append("      .with(this, %s", localName);
    if (!params.isEmpty()) {
      writer.append(", ");
      for (int i = 0, size = params.size(); i < size; i++) {
        if (i > 0) {
          writer.append(", ");
        }
        writer.append(params.get(i).simpleName());
      }
    }
    writer.append(")");
    int aspectCount = aspectPairs.size();
    if (aspectCount < 2) {
      writer.append(";").eol();
    } else {
      // nesting all but last aspect
      writer.eol();
      writer.append("      // wrapping inner nested aspects based on ordering attribute").eol();
      int nesting = aspectCount - 1;
      for (int i = 0; i < nesting; i++) {
        AspectPair aspect = aspectPairs.get(i);
        String sn = aspect.annotationShortName();
        writer.append("      .wrap(%s%s)", localName, sn);
        if (i < nesting -1) {
          writer.eol();
        } else {
          writer.append(";").eol();
        }
      }
    }
    writer.append("    try {").eol();
    if (aspectCount > 1) {
      writer.append("      // outer-most aspect").eol();
    }
    AspectPair outerAspect = aspectPairs.get(aspectCount - 1);
    String sn = outerAspect.annotationShortName();
    writer.append("      %s%s.invoke(call);", localName, sn).eol();

    if (!isVoid()) {
      writer.append("      return call.finalResult();").eol();
    }

    writeThrowsCatch(writer);
    writer.append("    } catch (Throwable e) {").eol();
    writer.append("      throw new InvocationException(e);").eol();
    writer.append("    }").eol();
  }

  private void writeThrowsCatch(Append writer) {
    writer.append("    } catch (");
    if (thrownTypes.isEmpty()) {
      writer.append("InvocationException");
    } else {
      writer.append("InvocationException");
      for (TypeMirror thrownType : thrownTypes) {
        writer.append(" | ").append(Util.shortName(thrownType.toString()));
      }
    }
    writer.append(" e) {").eol();
    writer.append("      throw e;").eol();
  }
}
