package org.example.myapp.external;

import io.avaje.inject.Component;
import io.avaje.jsonb.Jsonb;
import org.example.external.aspect.MyExternalAspect;
import org.other.one.OtherComponent;

@Component
public class HasExternalDependencies {

  // plugin
  public final Jsonb fromPlugin;
  // component from other module
  public final OtherComponent fromExternal;

  public HasExternalDependencies(Jsonb jsonb, OtherComponent otherComponent) {
    this.fromPlugin = jsonb;
    this.fromExternal = otherComponent;
  }

  @MyExternalAspect
  public String doStuff() {
    return "hello";
  }
}
