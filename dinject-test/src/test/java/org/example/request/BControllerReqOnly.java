package org.example.request;

import io.avaje.http.api.Controller;
import io.helidon.webserver.ServerRequest;

/**
 * Controller with request scoped dependencies (request and response).
 * <p>
 * The request scoped dependencies are automatically detected and a
 * BeanFactory2 implementation is generated instead of singleton di.
 */
@Controller
public class BControllerReqOnly {

  final AService service;
  final ServerRequest request;

  BControllerReqOnly(AService service, ServerRequest request) {
    this.service = service;
    this.request = request;
  }

  public String get() {
    return "hi " + request.toString() + service.hi();
  }
}
