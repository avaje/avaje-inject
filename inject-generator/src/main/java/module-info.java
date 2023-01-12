module io.avaje.inject.generator {

  requires java.compiler;
  requires io.avaje.inject;

  uses io.avaje.inject.spi.Plugin;

  provides javax.annotation.processing.Processor with io.avaje.inject.generator.Processor;
}
