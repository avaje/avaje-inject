module io.avaje.inject.test {

  exports io.avaje.inject.test;

  requires transitive io.avaje.inject;
  requires transitive org.junit.jupiter.engine;
  requires transitive org.junit.jupiter.api;
  requires static org.apiguardian.api; // needed for javadoc
  requires transitive org.mockito;
  requires transitive org.mockito.junit.jupiter;
//  requires transitive org.assertj.core;
//  requires transitive net.bytebuddy;
  requires static java.net.http; // for testing only
  requires static io.avaje.inject.aop;
  requires static io.avaje.inject.events;

  uses io.avaje.inject.test.TestModule;
  uses io.avaje.inject.test.Plugin;
  uses io.avaje.inject.test.LookupProvider;
}
