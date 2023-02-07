module io.avaje.inject.generator {

  requires java.compiler;
  requires static io.avaje.inject;
  requires static hickory;

  uses io.avaje.inject.spi.Plugin;
  uses io.avaje.inject.spi.Module;

  provides javax.annotation.processing.Processor with io.avaje.inject.generator.Processor;
}
