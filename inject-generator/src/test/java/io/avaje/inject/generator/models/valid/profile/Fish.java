package io.avaje.inject.generator.models.valid.profile;

public interface Fish {

  public class CardinalTetra implements Fish {

    @Override
    public String toString() {
      return "CardinalTetra";
    }
  }

  public class Discus implements Fish {

    @Override
    public String toString() {
      return "Discus";
    }
  }

  public class Betta implements Fish {

    @Override
    public String toString() {
      return "Betta";
    }
  }
}
