package org.example.myapp.named;

import jakarta.inject.Singleton;

@Singleton
@Accepts(PaymentMethod.MASTERCARD)
public class MasterCardPayStore implements PayStore {
}
