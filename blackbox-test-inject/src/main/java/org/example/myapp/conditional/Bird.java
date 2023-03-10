package org.example.myapp.conditional;

public interface Bird {

  public class Cassowary implements Bird {

    @Override
    public String toString() {
      return "Cassowary";
    }
  }

  public class Jay implements Bird {

    @Override
    public String toString() {
      return "Blue Jay";
    }
  }

}
