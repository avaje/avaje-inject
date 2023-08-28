module blackbox.aspect {

  requires io.avaje.inject;

  //remove this and compilation fails
  provides io.avaje.inject.spi.Module with org.example.external.aspect.sub.ExampleExternalAspectModule;

}
