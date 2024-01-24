package org.example.myapp.assist.droid;

public interface DroidFactory {

  Droid createDroid(int personality, Model model);

  interface Droid {

    int personality();

    Model model();

    boolean dependenciesAreWired();
  }
}