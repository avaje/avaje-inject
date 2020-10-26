package org.example.request;


import io.avaje.http.api.Controller;
import io.avaje.jex.Context;

import javax.inject.Inject;

@Controller
public class JexController {

  @Inject
  AService service;

  @Inject
  Context context;

  public String get() {
    return "hi " + context.toString() + service.hi();
  }
}
