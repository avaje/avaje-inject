module io.avaje.inject {

  exports io.avaje.inject;
  exports io.avaje.inject.core;

  requires transitive javax.inject;
  requires transitive org.slf4j;
  requires static org.mockito;

  uses io.avaje.inject.core.BeanContextFactory;

}
