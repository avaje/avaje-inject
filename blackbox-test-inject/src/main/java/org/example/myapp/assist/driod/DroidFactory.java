package org.example.myapp.assist.driod;

public interface DroidFactory {

  Droid createDroid(int personality, Model model);

  interface Droid {

  }
}
