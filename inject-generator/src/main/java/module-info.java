import io.avaje.inject.generator.InjectProcessor;
import io.avaje.inject.generator.InjectTestProcessor;

module io.avaje.inject.generator {

  requires java.compiler;
  requires io.avaje.inject;

  requires static io.avaje.inject.aop;
  requires static io.avaje.inject.events;
  requires static io.avaje.http.api;
  requires static io.avaje.prism;
  requires static io.avaje.spi;

  uses io.avaje.inject.spi.InjectExtension;

  provides javax.annotation.processing.Processor with InjectProcessor, InjectTestProcessor;
}
