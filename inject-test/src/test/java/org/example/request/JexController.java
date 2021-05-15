package org.example.request;


import io.avaje.http.api.Controller;
import io.avaje.jex.Context;
import jakarta.inject.Inject;

@Controller
public class JexController {

  @Inject
  AService service;

  Context context;

  @Inject
  JexController withContext(Context context) {
    this.context = context;
    return this;
  }

  public String get() {
    return "hi " + context.toString() + service.hi();
  }
}
