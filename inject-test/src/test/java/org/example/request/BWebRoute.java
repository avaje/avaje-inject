package org.example.request;

import io.avaje.inject.spi.BeanFactory2;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;

import javax.inject.Singleton;

/**
 * Simulate web route generation for Helidon.
 */
@Singleton
class BWebRoute {

  final BeanFactory2<BController, ServerRequest, ServerResponse> controllerFactory;

  BWebRoute(BeanFactory2<BController, ServerRequest, ServerResponse> controllerFactory) {
    this.controllerFactory = controllerFactory;
  }

  String get(ServerRequest request, ServerResponse response) {
    return controllerFactory.create(request, response).get();
  }
}
