import org.example.external.aspect.spi.AspectPlugin;

module blackbox.aspect {

  exports org.example.external.aspect;
  exports org.example.external.aspect.sub;

  requires io.avaje.inject;
  requires io.avaje.inject.aop;

  provides io.avaje.inject.spi.InjectExtension with AspectPlugin, org.example.external.aspect.sub.ExampleExternalAspectModule;

}
