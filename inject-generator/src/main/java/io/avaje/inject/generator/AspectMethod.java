package io.avaje.inject.generator;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class AspectMethod {

  private final List<AspectPair> aspectPairs;
  private final ExecutableElement method;
  private final List<MethodReader.MethodParam> params;
  private final String rawReturn;
  private final String simpleName;
  private final List<? extends TypeMirror> thrownTypes;
  private final String localName;

  AspectMethod(int nameIndex, List<AspectPair> aspectPairs, ExecutableElement method) {
    this.aspectPairs = aspectPairs;
    this.method = method;
    this.simpleName = method.getSimpleName().toString();
    this.params = initParams(method.getParameters());
    this.rawReturn = method.getReturnType().toString();
    this.thrownTypes = method.getThrownTypes();
    this.localName = simpleName + nameIndex;
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
      targets.add(aspectPair.target());
    }
  }

  boolean isVoid() {
    return rawReturn.equals("void");
  }

  void addImports(Set<String> importTypes) {
    for (AspectPair aspect : aspectPairs) {
      aspect.addImports(importTypes);
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
      params.get(i).writeMethodParam(writer);
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
    writer.append("  private Method %s;", localName).eol();
    for (AspectPair aspectPair : aspectPairs) {
      String sn = aspectPair.annotationShortName();
      writer.append("  private MethodInterceptor %s%s;", localName, sn).eol();
    }
  }

  void writeSetupForMethods(Append writer, String shortName) {
    writer.append("      %s = %s.class.getDeclaredMethod(\"%s\"", localName, shortName, simpleName);
    for (MethodReader.MethodParam param : params) {
      writer.append(", ");
      param.writeMethodParamType(writer);
      writer.append(".class");
    }
    writer.append(");").eol();
  }

  void writeSetupForMethodsInterceptor(Append writer) {
    for (AspectPair aspect : aspectPairs) {
      String target = aspect.target();
      String name = aspectTargetShortName(target);
      String sn = aspect.annotationShortName();
      writer.append("      %s%s = %s.interceptor(%s, %s.getAnnotation(%s.class));", localName, sn, name, localName, localName, sn).eol();
    }
  }

  static String aspectTargetShortName(String target) {
    String type = Util.shortName(target);
    return Util.initLower(type);
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
    writer.append("      .method(%s);", localName).eol();
    writer.append("    try {").eol();

    for (AspectPair aspect : aspectPairs) {
      String sn = aspect.annotationShortName();
      writer.append("      %s%s.invoke(call);", localName, sn).eol();
    }

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
