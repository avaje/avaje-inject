package org.example.myapp.named;

import jakarta.inject.Singleton;

@Singleton
@Accepts(PaymentMethod.MixedCase)
public class MixedCasePayStore implements PayStore {
}
