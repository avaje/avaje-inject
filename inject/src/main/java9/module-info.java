module io.avaje.inject {

  exports io.avaje.inject;
  exports io.avaje.inject.core;

  requires javax.inject;
  requires org.slf4j;
  requires static org.mockito;

  uses io.avaje.inject.core.BeanContextFactory;

}
