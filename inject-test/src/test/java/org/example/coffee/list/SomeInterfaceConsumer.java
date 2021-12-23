package org.example.coffee.list;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;

@Singleton
public class SomeInterfaceConsumer {
  private List<SomeInterface> list;

  @Inject
  public SomeInterfaceConsumer(List<SomeInterface> list) {
    this.list = list;
  }

  public List<SomeInterface> getList() {
    return list;
  }
}
