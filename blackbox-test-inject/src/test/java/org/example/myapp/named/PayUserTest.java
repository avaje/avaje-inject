package org.example.myapp.named;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;

import static org.assertj.core.api.Assertions.assertThat;

class PayUserTest {

  @Test
  void qualifierMap() {
    try (BeanScope beanScope = BeanScope.builder().build()) {

      var payUser = beanScope.get(PayUser.class);
      assertThat(payUser.keys()).contains("Accepts(value=MASTERCARD)", "Accepts(value=VISA)");

      // string manipulation into an EnumMap assumes ... *(value=<EnumKey>)
      var asEnumMap = new EnumMap<PaymentMethod,PayStore>(PaymentMethod.class);

      for (var entry : payUser.entries()) {
        String key = entry.getKey();
        int open = key.indexOf("(value=");
        int close = key.lastIndexOf(')');
        String enumKey = key.substring(open + 7, close);
        PaymentMethod paymentMethod = PaymentMethod.valueOf(enumKey);
        asEnumMap.put(paymentMethod, entry.getValue());
      }

      PayStore payStore = asEnumMap.get(PaymentMethod.MixedCase);
      assertThat(payStore).isInstanceOf(MixedCasePayStore.class);

      PayStore mcStore = asEnumMap.get(PaymentMethod.MASTERCARD);
      assertThat(mcStore).isInstanceOf(MasterCardPayStore.class);

      PayStore visaStore = asEnumMap.get(PaymentMethod.VISA);
      assertThat(visaStore).isInstanceOf(VisaPayStore.class);
    }
  }
}
