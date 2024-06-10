module blackbox.aspect {

  requires io.avaje.inject;
  requires io.avaje.inject.aop;

  //remove this and compilation fails
  provides io.avaje.inject.spi.InjectSPI with org.example.external.aspect.sub.ExampleExternalAspectModule;

}
