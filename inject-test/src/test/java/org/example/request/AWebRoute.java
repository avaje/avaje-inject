package org.example.request;

import io.avaje.inject.spi.BeanFactory;
import io.javalin.http.Context;

import javax.inject.Singleton;

/**
 * Simulate approx a Javalin web route with request scoped context.
 * Aka using BeanFactory that takes the context to create the controller.
 */
@Singleton
class AWebRoute {

  final BeanFactory<AController, Context> controllerFactory;

  AWebRoute(BeanFactory<AController, Context> controllerFactory) {
    this.controllerFactory = controllerFactory;
  }

  String get(Context ctx) {
    return controllerFactory.create(ctx).get();
  }
}
