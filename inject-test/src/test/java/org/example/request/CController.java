package org.example.request;

import io.avaje.http.api.Controller;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import org.example.generic.MyObj;
import org.example.generic.ReadService;

/**
 * Controller with request scoped dependencies and generic parameter.
 */
@Controller
public class CController {

  final ReadService<MyObj, Integer> readService;
  final ServerRequest request;
  final ServerResponse response;

  CController(ReadService<MyObj, Integer> readService, ServerRequest request, ServerResponse response) {
    this.readService = readService;
    this.request = request;
    this.response = response;
  }

  public String get() {
    return "hi " + request.toString() + response.toString() + readService.toString();
  }
}
