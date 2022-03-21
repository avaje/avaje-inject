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

  uses io.avaje.inject.test.TestModule;
}
