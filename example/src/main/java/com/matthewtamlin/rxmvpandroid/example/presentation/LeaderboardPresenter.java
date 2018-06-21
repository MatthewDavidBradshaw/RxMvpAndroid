package com.matthewtamlin.rxmvpandroid.example.presentation;

import android.support.annotation.NonNull;

import com.google.common.base.Optional;
import com.matthewtamlin.rxmvpandroid.RxMvpPresenter;
import com.matthewtamlin.rxmvpandroid.example.data.Player;
import com.matthewtamlin.rxmvpandroid.example.data.PlayerRepository;
import com.matthewtamlin.rxmvpandroid.example.view.LeaderboardView;
import com.matthewtamlin.rxmvpandroid.example.view.LeaderboardView.PlayerViewModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Presents data from a {@link PlayerRepository} to a {@link LeaderboardView}.
 */
public class LeaderboardPresenter implements RxMvpPresenter {
  private final PlayerRepository playerRepository;

  private final LeaderboardView leaderboardView;

  public LeaderboardPresenter(final PlayerRepository playerRepository, final LeaderboardView leaderboardView) {
    this.playerRepository = checkNotNull(playerRepository);
    this.leaderboardView = checkNotNull(leaderboardView);
  }

  @NonNull
  @Override
  public Completable createPresentationTasks() {
    return Completable.mergeArray(displayPlayers(), deletePlayers());
  }

  @NonNull
  @Override
  public Observable<Optional<Completable>> observePendingBackActions() {
    return Observable.never(); // Doesn't handle back presses
  }

  private Completable displayPlayers() {
    return playerRepository
        .observePlayers()
        .flatMapCompletable(players -> Observable
            .fromIterable(players)
            .sorted((player1, player2) -> Integer.compare(player2.getHighscore(), player1.getHighscore()))
            .map(this::toViewModel)
            .collectInto(new ArrayList<PlayerViewModel>(), List::add)
            .flatMapCompletable(leaderboardView::setPlayers));
  }

  private Completable deletePlayers() {
    return leaderboardView
        .observeDeleteRequests()
        .map(this::toDataModel)
        .flatMapCompletable(playerRepository::removePlayer);
  }

  private PlayerViewModel toViewModel(final Player player) {
    return PlayerViewModel.create(player.getName(), Integer.toString(player.getHighscore()));
  }

  private Player toDataModel(final PlayerViewModel playerViewModel) {
    return Player.create(playerViewModel.getName(), Integer.parseInt(playerViewModel.getHighscore()));
  }
}