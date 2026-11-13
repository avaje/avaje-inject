package org.example.myapp.autofactory;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
public class MapperUser {

  private final UserMapper userMapper;
  private final OrderMapper orderMapper;
  private final OrderMapper.Nested nested;

  MapperUser(UserMapper userMapper, @Named("orders") OrderMapper orderMapper, OrderMapper.Nested nested) {
    this.userMapper = userMapper;
    this.orderMapper = orderMapper;
    this.nested = nested;
  }

  String describe() {
    return userMapper.findById(42) + " | " + orderMapper.findByCustomer("bob") + " | " + nested.nested("x");
  }
}
