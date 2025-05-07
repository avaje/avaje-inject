package org.example.myapp.arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.example.myapp.arrays.ArrayFactory.ArrayType;
import org.junit.jupiter.api.Test;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;

@InjectTest
class ArraysTest {
  @Inject byte[] arr;
  @Inject ArrayType[] arr2;

  @Test
  void test() {

    assertNotNull(arr);
    assertNotNull(arr2);
  }
}
