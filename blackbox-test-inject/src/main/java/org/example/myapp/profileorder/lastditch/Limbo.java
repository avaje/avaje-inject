package org.example.myapp.profileorder.lastditch;

import io.avaje.inject.Primary;
import jakarta.inject.Singleton;

/** The first circle, so it descends from nothing. */
@Primary
@Singleton
public class Limbo implements Circle {}
