module io.avaje.inject {

  exports io.avaje.inject;
  exports io.avaje.inject.aop;
  exports io.avaje.inject.spi;

  requires transitive io.avaje.lang;
  requires transitive io.avaje.applog;
  requires transitive jakarta.inject;
  requires static org.mockito;

  uses io.avaje.inject.spi.Module;

}
