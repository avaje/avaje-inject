package io.avaje.inject.generator;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import java.util.*;
import io.avaje.inject.generator.MethodReader.MethodParam;

final class AspectMethod {

  private final List<AspectPair> aspectPairs;
  private final ExecutableElement method;
  private final List<MethodReader.MethodParam> params;
  private final UType returnUtype;
  private final String simpleName;
  private final List<? extends TypeMirror> thrownTypes;
  private final String localName;
  private final ExecutableElement fallback;
  private final boolean methodRef;

  AspectMethod(int nameIndex, List<AspectPair> aspectPairs, ExecutableElement method) {
    this.aspectPairs = sort(aspectPairs);
    this.method = method;
    this.simpleName = method.getSimpleName().toString();
    this.params = initParams(method.getParameters());
    this.returnUtype = UType.parse(method.getReturnType());
    this.thrownTypes = method.getThrownTypes();
    this.localName = simpleName + nameIndex;
    this.fallback = findFallback(method);
    methodRef = params.isEmpty();
    validateFallback();
  }

  private ExecutableElement findFallback(ExecutableElement method) {
    var methods = ElementFilter.methodsIn(method.getEnclosingElement().getEnclosedElements());
    var sameNameMethods = methods.stream().filter(m -> m.getSimpleName().contentEquals(simpleName)).collect(toList());
    var index = sameNameMethods.indexOf(method);

    return methods.stream()
      .filter(e -> matchFallback(e, sameNameMethods, index))
      .findFirst()
      .orElse(null);
  }

  private boolean matchFallback(ExecutableElement e, List<ExecutableElement> sameNameMethods, int index) {
    return AOPFallbackPrism.getOptionalOn(e)
      .filter(v -> matchFallback(v, sameNameMethods, index))
      .isPresent();
  }

  private boolean matchFallback(AOPFallbackPrism v, List<ExecutableElement> sameNameMethods, int index) {
    return v.value().contains(simpleName) && (sameNameMethods.size() == 1 || index == v.place());
  }

  private void validateFallback() {
    if (fallback == null) {
      return;
    }
    var returnType = UType.parse(fallback.getReturnType()).fullWithoutAnnotations();
    if (!returnType.equals(returnUtype.fullWithoutAnnotations())) {
      APContext.logError(fallback, "An AOP fallback method must have the same return type as the target method");
    }

    var fallParams = fallback.getParameters();
    final var size = fallParams.size();
    if (fallParams.isEmpty() || size == 1 && fallParams.get(0).asType().toString().contains("Throwable")) {
      return;
    }

    if (params.size() != size && size != params.size() + 1) {
      APContext.logError(
          fallback,
          "Invalid fallback signature. An AOP fallback method can have either 0 arguments, one Throwable argument, all the target method's arguments, or all the target method's arguments with Throwable appended.");
    }
  }

  private List<AspectPair> sort(List<AspectPair> aspectPairs) {
    Collections.sort(aspectPairs);
    return aspectPairs;
  }

  List<MethodReader.MethodParam> initParams(List<? extends VariableElement> parameters) {
    List<MethodReader.MethodParam> mps = new ArrayList<>(parameters.size());
    for (var e : parameters) {
      mps.add(new MethodReader.MethodParam(e));
    }
    return mps;
  }

  void addTargets(Set<String> targets) {
    for (AspectPair aspectPair : aspectPairs) {
      targets.add(aspectPair.annotationShortName());
    }
  }

  boolean isVoid() {
    return returnUtype.kind() == TypeKind.VOID;
  }

  void addImports(ImportTypeMap importTypes) {
    importTypes.addAll(returnUtype.importTypes());
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
    writer.append("  public %s %s(", returnUtype.shortType(), simpleName);
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

    writer.append("    var call = new Invocation.%s(", type);
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
    if (methodRef) {
      writer.append("super::%s", simpleName);
      return;
    }

    writer.append("() -> super.%s(", simpleName);
    for (int i = 0, size = params.size(); i < size; i++) {
      if (i > 0) {
        writer.append(", ");
      }
      writer.append(params.get(i).simpleName());
    }
    writer.append(")");
  }

  void writeSetupFields(Append writer, boolean lazy) {
    var isFinal = lazy ? "" : "final ";
    writer.append("  private %sMethod %s;", isFinal, localName).eol();
    for (AspectPair aspectPair : aspectPairs) {
      String sn = aspectPair.annotationShortName();
      writer.append("  private %sMethodInterceptor %s%s;", isFinal, localName, sn).eol();
    }
  }

  void writeSetupForMethods(Append writer) {
    writer.append("      %s = target$Class.getDeclaredMethod(\"%s\"", localName, simpleName);
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
    if (aspectCount >= 2) {
      // nesting all but last aspect
      writer.eol();
      writer.append("      // wrapping inner nested aspects based on ordering attribute").eol();
      int nesting = aspectCount - 1;
      for (int i = 0; i < nesting; i++) {
        AspectPair aspect = aspectPairs.get(i);
        String sn = aspect.annotationShortName();
        writer.append("      .wrap(%s%s)", localName, sn);
        if (i < nesting - 1) {
          writer.eol();
        }
      }
    }
    writeFallback(writer);
    writer.append(";").eol();
    writer.append("    try {").eol();
    if (aspectCount > 1) {
      writer.append("      // outer-most aspect").eol();
    }
    final AspectPair outerAspect = aspectPairs.get(aspectCount - 1);
    final String sn = outerAspect.annotationShortName();
    writer.append("      %s%s.invoke(call);", localName, sn).eol();

    if (!isVoid()) {
      writer.append("      return call.finalResult();").eol();
    }

    writer.append("    } catch (RuntimeException $ex) {").eol();
    writer.append("      $ex.addSuppressed(new InvocationException(\"%s proxy threw exception\"));", simpleName).eol();
    writer.append("      throw $ex;").eol();
    writeThrowsCatch(writer);
    if (thrownTypes.stream().map(Object::toString).noneMatch("java.lang.Throwable"::equals)) {
      writer.append("    } catch (Throwable $t) {").eol();
      writer.append("      throw new InvocationException(\"%s proxy threw exception\", $t);", simpleName).eol();
    }
    writer.append("    }").eol();
  }

  private void writeFallback(Append writer) {
    if (fallback == null) {
      return;
    }

    var fallParams = fallback.getParameters();
    writer.eol().append("      .fallback(");
    var hasThrowable = fallParams.stream().anyMatch(p -> p.asType().toString().contains("Throwable"));

    if (fallParams.size() == 1 && hasThrowable) {
      writer.append("this::%s", fallback.getSimpleName());
      writer.append(")");
      return;
    }

    var jdk = APContext.jdkVersion();
    var unNamedPattern = jdk >= 22 || APContext.previewEnabled() && jdk == 21;

    var exName = !hasThrowable && unNamedPattern ? "_" : "$ex";
    writer.append("%s -> %s(", exName, fallback.getSimpleName());
    if (!fallParams.isEmpty()) {
      for (int i = 0, size = params.size(); i < size; i++) {
        if (i > 0) {
          writer.append(", ");
        }
        writer.append(params.get(i).simpleName());
      }
    }
    if (hasThrowable) {
      writer.append(", $ex");
    }
    writer.append("))");
  }

  private void writeThrowsCatch(Append writer) {
    final var types = new ArrayList<>(thrownTypes);
    types.removeIf(ProcessingContext::isUncheckedException);
    if (types.isEmpty()) {
      return;
    }
    writer.append("    } catch (");
    types.stream()
        .map(Object::toString)
        .map(Util::shortName)
        .collect(collectingAndThen(joining(" | "), writer::append))
        .append(" e) {")
        .eol();
    writer.append("      e.addSuppressed(new InvocationException(\"%s proxy threw exception\"));", simpleName).eol();
    writer.append("      throw e;").eol();
  }
}
