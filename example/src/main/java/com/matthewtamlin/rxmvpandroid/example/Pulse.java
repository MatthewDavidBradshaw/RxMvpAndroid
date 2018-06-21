package com.matthewtamlin.rxmvpandroid.example;

/**
 * An event without data. Implemented as a singleton for performance.
 */
public class Pulse {
  private static final Pulse INSTANCE = new Pulse();

  public static Pulse getInstance() {
    return INSTANCE;
  }

  private Pulse() {}
}