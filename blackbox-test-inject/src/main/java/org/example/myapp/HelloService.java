package org.example.myapp;

import jakarta.inject.Singleton;

@Singleton
public class HelloService {

  private final HelloData data;

  HelloService(HelloData data) {
    this.data = data;
  }

  public String hello() {
    return "hello+" + data.helloData();
  }
}
