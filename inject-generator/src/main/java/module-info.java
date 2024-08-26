module io.avaje.inject.generator {

  requires java.compiler;
  requires io.avaje.inject;
  requires io.avaje.inject.aop;
  requires io.avaje.inject.events;

  requires static io.avaje.prism;

  uses io.avaje.inject.spi.InjectExtension;

  provides javax.annotation.processing.Processor with io.avaje.inject.generator.InjectProcessor;
}
