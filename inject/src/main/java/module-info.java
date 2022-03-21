module io.avaje.inject {

  exports io.avaje.inject;
  exports io.avaje.inject.aop;
  exports io.avaje.inject.spi;

  requires transitive io.avaje.lang;
  requires transitive java.inject;
  requires static org.mockito;

  uses io.avaje.inject.spi.Module;

}
