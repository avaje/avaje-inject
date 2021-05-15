package io.avaje.inject.generator;

import java.util.Set;

/**
 * Holds detection and details of request scoped dependencies.
 */
class BeanRequestParams {

  private final String parentType;

  private RequestScope.Handler reqScopeHandler;

  BeanRequestParams(String parentType) {
    this.parentType = parentType;
  }

  /**
   * Return true if this type is a request scoped type (e.g. Javalin Context).
   */
  boolean check(String paramType) {
    if (paramType != null && RequestScope.check(paramType)) {
      if (reqScopeHandler == null) {
        reqScopeHandler = RequestScope.handler(paramType);
      }
      return true;
    }
    return false;
  }

  /**
   * Return true if the bean has request scoped dependencies.
   */
  boolean isRequestScopedController() {
    return reqScopeHandler != null;
  }

  void factoryInterface(Append writer) {
    reqScopeHandler.factoryInterface(writer, nm(parentType));
  }

  void addImports(Set<String> importTypes) {
    if (reqScopeHandler != null) {
      importTypes.add(Constants.SINGLETON);
      importTypes.add(Constants.INJECT);
      reqScopeHandler.addImports(importTypes);
    }
  }

  void writeRequestCreate(Append writer) {
    writer.append("  @Override").eol();
    reqScopeHandler.writeCreateMethod(writer, nm(parentType));
  }

  /**
   * Return the argument name based on the parameter type.
   */
  String argumentName(String paramType) {
    return reqScopeHandler.argumentName(paramType);
  }

  private String nm(String raw) {
    return Util.shortName(raw);
  }
}
