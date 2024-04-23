package org.example.myapp.named;

import jakarta.inject.Singleton;

@Singleton
@Accepts(PaymentMethod.VISA)
public class VisaPayStore implements PayStore {
}
