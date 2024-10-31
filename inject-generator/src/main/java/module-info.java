module io.avaje.inject.generator {

  requires java.compiler;
  requires io.avaje.inject;
  requires io.avaje.inject.aop;
  requires io.avaje.inject.events;

  requires static io.avaje.prism;
  requires static io.avaje.spi;

  uses io.avaje.inject.spi.InjectExtension;
  uses io.avaje.inject.spi.Plugin;
  uses io.avaje.inject.spi.Module;
  uses io.avaje.inject.spi.InjectPlugin;
  uses io.avaje.inject.spi.AvajeModule;

  provides javax.annotation.processing.Processor with io.avaje.inject.generator.InjectProcessor;
}
