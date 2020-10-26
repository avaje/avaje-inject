package io.avaje.inject.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Handling detection of request scoped dependencies and appropriate BeanFactory generation.
 */
class RequestScope {

  private static final String JEX_CONTEXT = "io.avaje.jex.Context";
  private static final String JAVALIN_CONTEXT = "io.javalin.http.Context";
  private static final String HELIDON_REQ = "io.helidon.webserver.ServerRequest";
  private static final String HELIDON_RES = "io.helidon.webserver.ServerResponse";
  private static final Map<String, Handler> TYPES = new HashMap<>();

  static {
    TYPES.put(JEX_CONTEXT, new Jex());
    TYPES.put(JAVALIN_CONTEXT, new Javalin());
    TYPES.put(HELIDON_REQ, new Helidon());
    TYPES.put(HELIDON_RES, new Helidon());
  }

  /**
   * Return true if the type is a request scoped type.
   */
  static boolean check(String type) {
    return TYPES.containsKey(type);
  }

  /**
   * Return the Handler given the request scoped type.
   */
  static Handler handler(String type) {
    return TYPES.get(type);
  }

  /**
   * Handle BeanFactory (request scoped) generation.
   */
  interface Handler {

    /**
     * Generate appropriate BeanFactory interface.
     */
    void factoryInterface(Append writer, String parentType);

    /**
     * Add appropriate imports.
     */
    void addImports(Set<String> importTypes);

    /**
     * Add dependencies and create method.
     */
    void writeCreateMethod(Append writer, String parentType);

    /**
     * Return the argument name based on the parameter type.
     */
    String argumentName(String paramType);
  }

  /**
   * Jex support for request scoping/BeanFactory.
   */
  private static class Jex implements Handler {

    @Override
    public void factoryInterface(Append writer, String parentType) {
      writer.append("BeanFactory<%s, %s>", parentType, "Context");
    }

    @Override
    public void addImports(Set<String> importTypes) {
      importTypes.add(Constants.BEAN_FACTORY);
      importTypes.add(JEX_CONTEXT);
    }

    @Override
    public void writeCreateMethod(Append writer, String parentType) {
      writer.append("  public %s create(Context context) {", parentType).eol();
    }

    @Override
    public String argumentName(String paramType) {
      return "context";
    }
  }

  /**
   * Javalin support for request scoping/BeanFactory.
   */
  private static class Javalin implements Handler {

    @Override
    public void factoryInterface(Append writer, String parentType) {
      writer.append("BeanFactory<%s, %s>", parentType, "Context");
    }

    @Override
    public void addImports(Set<String> importTypes) {
      importTypes.add(Constants.BEAN_FACTORY);
      importTypes.add(JAVALIN_CONTEXT);
    }

    @Override
    public void writeCreateMethod(Append writer, String parentType) {
      writer.append("  public %s create(Context context) {", parentType).eol();
    }

    @Override
    public String argumentName(String paramType) {
      return "context";
    }
  }

  /**
   * Helidon support for request scoping/BeanFactory.
   */
  private static class Helidon implements Handler {

    @Override
    public void factoryInterface(Append writer, String parentType) {
      writer.append("BeanFactory2<%s, %s, %s>", parentType, "ServerRequest", "ServerResponse");
    }

    @Override
    public void addImports(Set<String> importTypes) {
      importTypes.add(Constants.BEAN_FACTORY2);
      importTypes.add(HELIDON_REQ);
      importTypes.add(HELIDON_RES);
    }

    @Override
    public void writeCreateMethod(Append writer, String parentType) {
      writer.append("  public %s create(ServerRequest request, ServerResponse response) {", parentType).eol();
    }

    @Override
    public String argumentName(String paramType) {
      if (paramType.equals(HELIDON_RES)) {
        return "response";
      } else {
        return "request";
      }
    }
  }
}
