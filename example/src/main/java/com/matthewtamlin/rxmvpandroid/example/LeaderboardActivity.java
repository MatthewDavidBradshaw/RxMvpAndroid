package com.matthewtamlin.rxmvpandroid.example;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.matthewtamlin.rxmvpandroid.RxMvpActivity;
import com.matthewtamlin.rxmvpandroid.example.data.InMemoryPlayerRepository;
import com.matthewtamlin.rxmvpandroid.example.data.PlayerRepository;
import com.matthewtamlin.rxmvpandroid.example.presentation.LeaderboardPresenter;
import com.matthewtamlin.rxmvpandroid.example.view.LeaderboardView;
import com.matthewtamlin.rxmvpandroid.librarytestharnesses.R;

/**
 * Hosts a {@link LeaderboardView} to display player high scores.
 */
public class LeaderboardActivity extends RxMvpActivity<LeaderboardView, LeaderboardPresenter> {
  private LeaderboardView leaderboardView;

  private LeaderboardPresenter leaderboardPresenter;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.leaderboard_activity);
    leaderboardView = findViewById(R.id.leaderboard);

    // Would probably inject this in a real scenario
    final PlayerRepository playerRepository = new InMemoryPlayerRepository();

    leaderboardPresenter = new LeaderboardPresenter(playerRepository, leaderboardView);
  }

  @NonNull
  @Override
  public LeaderboardView getView() {
    return leaderboardView;
  }

  @NonNull
  @Override
  public LeaderboardPresenter getPresenter() {
    return leaderboardPresenter;
  }
}