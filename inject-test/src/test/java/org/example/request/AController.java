package org.example.request;


import io.avaje.http.api.Controller;
import io.javalin.http.Context;

import javax.inject.Inject;

@Controller
public class AController {

  @Inject
  AService service;

  Context context;

  @Inject
  AController withContext(Context context) {
    this.context = context;
    return this;
  }

  public String get() {
    return "hi " + context.toString() + service.hi();
  }
}
