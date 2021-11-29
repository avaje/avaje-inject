package org.example.inheritprovides;

import javax.inject.Singleton;

import java.util.Map;

@Singleton
public class Controller2 extends Controller {

  @Override
  Map<String, String> getContext() {
    Map<String, String> context = super.getContext();
    context.put("something", "2");
    return context;
  }

}
