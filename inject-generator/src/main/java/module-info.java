module io.avaje.inject.generator {

  requires java.compiler;
  requires io.avaje.inject;
  requires static io.avaje.prism;

  uses io.avaje.inject.spi.InjectSPI;
  uses io.avaje.inject.spi.Plugin;
  uses io.avaje.inject.spi.Module;
  uses io.avaje.inject.spi.InjectPlugin;
  uses io.avaje.inject.spi.AvajeModule;

  provides javax.annotation.processing.Processor with io.avaje.inject.generator.InjectProcessor;
}
