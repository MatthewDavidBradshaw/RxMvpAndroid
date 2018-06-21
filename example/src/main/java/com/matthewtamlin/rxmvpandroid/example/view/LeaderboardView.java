package com.matthewtamlin.rxmvpandroid.example.view;

import com.google.auto.value.AutoValue;
import com.matthewtamlin.rxmvpandroid.RxMvpView;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * Displays players' names and highscores.
 */
public interface LeaderboardView extends RxMvpView {
  public Completable setPlayers(final List<PlayerViewModel> players);

  public Observable<PlayerViewModel> observeDeleteRequests();

  @AutoValue
  public static abstract class PlayerViewModel {
    public abstract String getName();

    public abstract String getHighscore();

    public static PlayerViewModel create(final String name, final String highscore) {
      return new AutoValue_LeaderboardView_PlayerViewModel(name, highscore);
    }
  }
}