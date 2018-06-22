package com.matthewtamlin.rxmvpandroid.example.data;

import com.google.auto.value.AutoValue;

/**
 * A player's name and their highscore.
 */
@AutoValue
public abstract class Player {
  public abstract String getName();

  public abstract int getHighscore();

  public static Player create(final String name, final int highscore) {
    return new AutoValue_Player(name, highscore);
  }
}
