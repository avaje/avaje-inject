package org.example.observes;

import java.util.ArrayDeque;

import org.example.coffee.prototype.MyProto;

import io.avaje.inject.events.Observes;
import jakarta.inject.Singleton;

@Singleton
public class MyObserverInjectPrototype {

  boolean invoked = false;
  CustomEvent event;
  ArrayDeque<MyProto> beansList = new ArrayDeque<>();

  void observe(@Observes CustomEvent e, MyProto proto) {
    invoked = true;
    event = e;
    beansList.add(proto);
  }
}
