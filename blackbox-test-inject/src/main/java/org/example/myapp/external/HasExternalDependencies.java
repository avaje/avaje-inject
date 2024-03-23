package org.example.myapp.external;

import io.avaje.inject.Component;
import io.avaje.jsonb.Jsonb;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.example.external.aspect.MyExternalAspect;
import org.other.one.OtherComponent;

@Component
public class HasExternalDependencies {

  // plugin
  public final Jsonb fromPlugin;
  // component from other module
  public final OtherComponent fromExternal;
  public final Supplier<String> stringSupplier;

  public HasExternalDependencies(
      Jsonb jsonb,
      OtherComponent otherComponent,
      Supplier<String> stringSupplier) {
    this.fromPlugin = jsonb;
    this.fromExternal = otherComponent;
    this.stringSupplier = stringSupplier;
  }

  @MyExternalAspect
  public String doStuff() {
    return stringSupplier.get();
  }
}
