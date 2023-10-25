package org.example.request;

import io.avaje.http.api.Controller;
import io.helidon.webserver.http.ServerResponse;

/**
 * Controller with request scoped dependencies (request and response).
 * <p>
 * The request scoped dependencies are automatically detected and a
 * BeanFactory2 implementation is generated instead of singleton di.
 */
@Controller
public class BControllerResOnly {

  final AService service;
  final ServerResponse response;

  BControllerResOnly(AService service, ServerResponse response) {
    this.service = service;
    this.response = response;
  }

  public String get() {
    return "hi " + response.toString() + service.hi();
  }
}
