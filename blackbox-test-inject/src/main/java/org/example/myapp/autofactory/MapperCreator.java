package org.example.myapp.autofactory;

import io.avaje.inject.AutoFactory;
import jakarta.inject.Singleton;
import org.example.myapp.autofactory.ext.InvoiceMapper;
import org.example.myapp.autofactory.ext.PaymentMapper;
import org.example.myapp.autofactory.ext.ShippingMapper;

@Singleton
public final class MapperCreator implements AutoFactory.Creator {

  private final Greeter greeter;

  MapperCreator(Greeter greeter) {
    this.greeter = greeter;
  }

  @Override
  public <T> T create(Class<T> type) {
    return type.cast(implementation(type));
  }

  private Object implementation(Class<?> type) {
    if (type == UserMapper.class) {
      return (UserMapper) id -> greeter.greet("user " + id);
    }
    if (type == OrderMapper.class) {
      return (OrderMapper) customer -> greeter.greet("orders of " + customer);
    }
    if (type == OrderMapper.Nested.class) {
      return (OrderMapper.Nested) value -> greeter.greet("nested " + value);
    } else if (type == PaymentMapper.class) {
      return (PaymentMapper) id -> greeter.greet("payment " + id);
    } else if (type == InvoiceMapper.class) {
      return (InvoiceMapper) id -> greeter.greet("invoice " + id);
    } else if (type == ShippingMapper.class) {
      return (ShippingMapper) id -> greeter.greet("shipment " + id);
    }
    throw new IllegalArgumentException("Unknown type " + type);
  }
}
