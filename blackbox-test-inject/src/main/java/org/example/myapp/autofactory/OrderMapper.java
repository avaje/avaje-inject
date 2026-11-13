package org.example.myapp.autofactory;

import io.avaje.inject.AutoFactory;
import io.avaje.inject.Prototype;
import jakarta.inject.Named;

@Named("orders")
@Prototype
@AutoFactory(MapperCreator.class)
public interface OrderMapper {

  String findByCustomer(String customer);

  @AutoFactory(MapperCreator.class)
  interface Nested {

    String nested(String value);
  }
}
