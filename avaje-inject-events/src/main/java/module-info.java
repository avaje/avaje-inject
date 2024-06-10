module io.avaje.inject.events {

  exports io.avaje.inject.events;

  requires transitive io.avaje.inject;
  requires static io.avaje.spi;
  provides io.avaje.inject.spi.InjectSPI with io.avaje.inject.events.spi.ObserverManagerPlugin;
}
