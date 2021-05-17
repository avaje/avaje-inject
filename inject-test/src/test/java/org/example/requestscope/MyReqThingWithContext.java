package org.example.requestscope;

import io.avaje.inject.Request;
import io.javalin.http.Context;

@Request
public class MyReqThingWithContext {

  final Context javalinContext;

  public MyReqThingWithContext(Context javalinContext) {
    this.javalinContext = javalinContext;
  }

}
