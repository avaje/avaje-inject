package io.avaje.inject.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Handling detection of request scoped dependencies and appropriate BeanFactory generation.
 */
final class RequestScope {

  private static final String JEX_CONTEXT = "io.avaje.jex.Context";
  private static final String JAVALIN_CONTEXT = "io.javalin.http.Context";
  private static final String HELIDON_REQ = "io.helidon.webserver.ServerRequest";
  private static final String HELIDON_RES = "io.helidon.webserver.ServerResponse";

  private static final String NIMA_REQ = "io.helidon.nima.webserver.http.ServerRequest";
  private static final String NIMA_RES = "io.helidon.nima.webserver.http.ServerResponse";
  private static final String HELIDON_REACTIVE_REQ = "io.helidon.reactive.webserver.ServerRequest";
  private static final String HELIDON_REACTIVE_RES = "io.helidon.reactive.webserver.ServerResponse";

  private static final Map<String, Handler> TYPES = new HashMap<>();
  static {
    TYPES.put(JEX_CONTEXT, new JexHandler());
    TYPES.put(JAVALIN_CONTEXT, new JavalinHandler());
    final var helidon = new Helidon();
    TYPES.put(HELIDON_REQ, helidon);
    TYPES.put(HELIDON_RES, helidon);
    final var helidonReactive = new HelidonReactive();
    TYPES.put(HELIDON_REACTIVE_REQ, helidonReactive);
    TYPES.put(HELIDON_REACTIVE_RES, helidonReactive);
    final var helidonNima = new HelidonNima();
    TYPES.put(NIMA_REQ, helidonNima);
    TYPES.put(NIMA_RES, helidonNima);
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
    void addImports(ImportTypeMap importTypes);

    /**
     * Add dependencies and create method.
     */
    void writeCreateMethod(Append writer, String parentType);

    /**
     * Return the argument name based on the parameter type.
     */
    String argumentName(String paramType);
  }


  private static final class JexHandler extends ContextHandler {
    private JexHandler() {
      super(JEX_CONTEXT);
    }
  }

  private static final class JavalinHandler extends ContextHandler {
    private JavalinHandler() {
      super(JAVALIN_CONTEXT);
    }
  }

  private static final class Helidon extends RequestResponseHandler {
    Helidon() {
      super(HELIDON_REQ, HELIDON_RES);
    }
  }

  private static final class HelidonReactive extends RequestResponseHandler {
    HelidonReactive() {
      super(HELIDON_REACTIVE_REQ, HELIDON_REACTIVE_RES);
    }
  }

  private static final class HelidonNima extends RequestResponseHandler {
    HelidonNima() {
      super(NIMA_REQ, NIMA_RES);
    }
  }


  /**
   * Single Context based handlers.
   */
  private static abstract class ContextHandler implements Handler {

    final String contextType;

    private ContextHandler(String contextType) {
      this.contextType = contextType;
    }

    @Override
    public void factoryInterface(Append writer, String parentType) {
      writer.append("BeanFactory<%s, %s>", parentType, "Context");
    }

    @Override
    public void addImports(ImportTypeMap importTypes) {
      importTypes.add(Constants.BEAN_FACTORY);
      importTypes.add(contextType);
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
   * ServerRequest ServerResponse based Handlers.
   */
  private static abstract class RequestResponseHandler implements Handler {

    final String reqType;
    final String resType;

    RequestResponseHandler(String reqType, String resType) {
      this.reqType = reqType;
      this.resType = resType;
    }

    @Override
    public void factoryInterface(Append writer, String parentType) {
      writer.append("BeanFactory2<%s, %s, %s>", parentType, "ServerRequest", "ServerResponse");
    }

    @Override
    public void addImports(ImportTypeMap importTypes) {
      importTypes.add(Constants.BEAN_FACTORY2);
      importTypes.add(reqType);
      importTypes.add(resType);
    }

    @Override
    public void writeCreateMethod(Append writer, String parentType) {
      writer.append("  public %s create(ServerRequest request, ServerResponse response) {", parentType).eol();
    }

    @Override
    public String argumentName(String paramType) {
      if (paramType.equals(resType)) {
        return "response";
      } else {
        return "request";
      }
    }
  }
}
