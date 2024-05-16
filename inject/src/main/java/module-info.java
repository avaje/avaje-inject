module io.avaje.inject {

  exports io.avaje.inject;
  exports io.avaje.inject.aop;
  exports io.avaje.inject.event;
  exports io.avaje.inject.spi;

  requires transitive io.avaje.lang;
  requires transitive io.avaje.applog;
  requires transitive jakarta.inject;
  requires static io.avaje.config;
  requires static org.mockito;

  uses io.avaje.inject.spi.AvajeModule;
  uses io.avaje.inject.spi.Module;
  uses io.avaje.inject.spi.ModuleOrdering;
  uses io.avaje.inject.spi.InjectPlugin;
  uses io.avaje.inject.spi.Plugin;
  uses io.avaje.inject.spi.PropertyRequiresPlugin;

}
