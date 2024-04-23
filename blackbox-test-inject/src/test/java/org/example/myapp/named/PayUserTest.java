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
      EnumMap<PaymentMethod,PayStore> asEnumMap = beanScope.enumMap(PaymentMethod.class, PayStore.class);

      PayStore payStore = asEnumMap.get(PaymentMethod.MixedCase);
      assertThat(payStore).isInstanceOf(MixedCasePayStore.class);

      PayStore mcStore = asEnumMap.get(PaymentMethod.MASTERCARD);
      assertThat(mcStore).isInstanceOf(MasterCardPayStore.class);

      PayStore visaStore = asEnumMap.get(PaymentMethod.VISA);
      assertThat(visaStore).isInstanceOf(VisaPayStore.class);
    }
  }
}
