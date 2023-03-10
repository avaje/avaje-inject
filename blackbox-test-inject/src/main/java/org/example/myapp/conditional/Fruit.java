package org.example.myapp.conditional;

public interface Fruit {
  public class Apple implements Fruit {

    @Override
    public String toString() {
      return "Apple";
    }
  }

  public class Mango implements Fruit {

    @Override
    public String toString() {
      return "Mango";
    }
  }
}
