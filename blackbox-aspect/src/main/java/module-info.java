module blackbox.aspect {

  exports org.example.external.aspect;

  requires io.avaje.inject;

  provides io.avaje.inject.spi.Module with org.example.external.aspect.sub.ExampleExternalAspectModule;
}
