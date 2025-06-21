package org.example.myapp.assist.generic;

public interface LordFactory<N extends NightLord> {
  N create(String title);
}
