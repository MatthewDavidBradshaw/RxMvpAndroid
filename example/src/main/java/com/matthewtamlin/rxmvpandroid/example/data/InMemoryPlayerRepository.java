package com.matthewtamlin.rxmvpandroid.example.data;

import com.google.common.collect.ImmutableSet;
import com.matthewtamlin.rxmvpandroid.example.Pulse;

import java.util.HashSet;
import java.util.Set;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;

/**
 * A PlayerRepository that stores all data in memory. Nothing is persisted.
 */
public class InMemoryPlayerRepository implements PlayerRepository {
  private static final Set<Player> INITIAL_PLAYERS = ImmutableSet.of(
      Player.create("Scout", 100),
      Player.create("Soldier", 120),
      Player.create("Pyro", 90),
      Player.create("Demoman", 85),
      Player.create("Heavy", 100),
      Player.create("Engineer", 140),
      Player.create("Medic", 180),
      Player.create("Sniper", 130),
      Player.create("Spy", 250));

  private final PublishSubject<Pulse> updated = PublishSubject.create();

  private Set<Player> currentPlayers = new HashSet<>();

  public InMemoryPlayerRepository() {
    // Hardcoding these values is fine for the demo but obviously not something we'd do in a real scenario
    currentPlayers.addAll(INITIAL_PLAYERS);
  }

  @Override
  public Observable<Set<Player>> observePlayers() {
    return updated
        .startWith(Pulse.getInstance())
        .flatMapSingle(pulse -> Single.just(currentPlayers));
  }

  @Override
  public Completable removePlayer(final Player player) {
    return Completable
        .fromRunnable(() -> currentPlayers.remove(player))
        .doOnComplete(() -> updated.onNext(Pulse.getInstance()));
  }
}