module io.avaje.inject.generator {

  requires java.compiler;
  requires io.avaje.inject;
  requires io.avaje.inject.prism;

  uses io.avaje.inject.spi.Plugin;
  uses io.avaje.inject.spi.Module;

  provides javax.annotation.processing.Processor with io.avaje.inject.generator.Processor;
}
