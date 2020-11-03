package org.example.request;

import io.avaje.http.api.Controller;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;

/**
 * Controller with request scoped dependencies (request and response).
 * <p>
 * The request scoped dependencies are automatically detected and a
 * BeanFactory2 implementation is generated instead of singleton di.
 */
@Controller
public class BController {

  final AService service;
  final ServerRequest request;
  final ServerResponse response;

  BController(AService service, ServerRequest request, ServerResponse response) {
    this.service = service;
    this.request = request;
    this.response = response;
  }

  public String get() {
    return "hi " + request.toString() + response.toString() + service.hi();
  }
}
