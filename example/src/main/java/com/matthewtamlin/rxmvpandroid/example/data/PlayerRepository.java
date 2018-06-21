package com.matthewtamlin.rxmvpandroid.example.data;

import java.util.Set;

import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * Stores a set of {@link Player}.
 */
public interface PlayerRepository {
  // A real repository would have create and update methods too, but this is sufficient for the example

  public Observable<Set<Player>> observePlayers();

  public Completable removePlayer(Player player);
}