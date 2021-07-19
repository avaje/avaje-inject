package io.avaje.inject.generator;


import java.util.Set;

/**
 * Holds detection and details of request scoped dependencies.
 */
class BeanRequestParams {

  private final ProcessingContext context;
  private final String parentType;
  private final boolean requestScopedBean;

  private RequestScope.Handler reqScopeHandler;

  BeanRequestParams(ProcessingContext context, String parentType, boolean requestScopedBean) {
    this.context = context;
    this.parentType = parentType;
    this.requestScopedBean = requestScopedBean;
  }

  /**
   * Return true if this type is a request scoped type (e.g. Javalin Context).
   */
  boolean check(String paramType) {
    if (requestScopedBean) {
      // Beans that are @Request don't get the reqScopeHandler factory generated
      return false;
    }
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
