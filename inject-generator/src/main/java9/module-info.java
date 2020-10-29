module io.avaje.inject.generator {

  requires java.compiler;
  requires javax.inject;
  requires java.annotation;
  requires io.avaje.inject;

  provides javax.annotation.processing.Processor with io.avaje.inject.generator.Processor;
}
