module io.avaje.inject.generator {

  requires java.compiler;
  requires io.avaje.inject;

  provides javax.annotation.processing.Processor with io.avaje.inject.generator.Processor;
}
