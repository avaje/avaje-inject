import io.avaje.inject.spi.InjectModule;

module io.avaje.inject.generator {

  requires java.compiler;
  requires io.avaje.inject;
  requires io.avaje.inject.aop;
  requires io.avaje.inject.events;

  requires static io.avaje.prism;

  uses io.avaje.inject.spi.InjectExtension;
  uses io.avaje.inject.spi.Plugin;
  uses io.avaje.inject.spi.Module;
  uses io.avaje.inject.spi.InjectPlugin;
  uses InjectModule;

  provides javax.annotation.processing.Processor with io.avaje.inject.generator.InjectProcessor;
}
