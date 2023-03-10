package org.example.myapp.conditional;

public interface Bird {

  public class Cassowary implements Bird {

    @Override
    public String toString() {
      return "Cassowary";
    }
  }

  public class BlueJay implements Bird {

    @Override
    public String toString() {
      return "BlueJay";
    }
  }

  public class StrawberryFinch implements Bird {

    @Override
    public String toString() {
      return "StrawBerryFinch";
    }
  }
}
