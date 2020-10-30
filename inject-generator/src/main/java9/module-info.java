module io.avaje.inject.generator {

  requires java.compiler;
  requires io.avaje.inject;
  requires java.annotation;

  provides javax.annotation.processing.Processor with io.avaje.inject.generator.Processor;
}
