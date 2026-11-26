package org.example.myapp.autofactory;

import io.avaje.inject.BeanScope;
import org.example.myapp.autofactory.ext.InvoiceMapper;
import org.example.myapp.autofactory.ext.PaymentMapper;
import org.example.myapp.autofactory.ext.ShippingMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AutoFactoryTest {

  @Test
  void interfaceBeansCreatedByCreator() {
    try (BeanScope scope = BeanScope.builder().build()) {
      UserMapper userMapper = scope.get(UserMapper.class);
      assertThat(userMapper.findById(1)).isEqualTo("hello user 1");
      assertThat(scope.get(UserMapper.class)).isSameAs(userMapper);

      MapperUser user = scope.get(MapperUser.class);
      assertThat(user.describe()).isEqualTo("hello user 42 | hello orders of bob | hello nested x");
    }
  }

  @Test
  void qualifierAndPrototypeCopied() {
    try (BeanScope scope = BeanScope.builder().build()) {
      OrderMapper one = scope.get(OrderMapper.class, "orders");
      OrderMapper two = scope.get(OrderMapper.class, "orders");
      assertThat(one).isNotSameAs(two);
    }
  }

  @Test
  void importedTypes() {
    try (BeanScope scope = BeanScope.builder().build()) {
      assertThat(scope.get(PaymentMapper.class).findPayment("p1")).isEqualTo("hello payment p1");
      assertThat(scope.get(InvoiceMapper.class).findInvoice("i1")).isEqualTo("hello invoice i1");
      assertThat(scope.get(ShippingMapper.class).findShipment("s1")).isEqualTo("hello shipment s1");
    }
  }
}
