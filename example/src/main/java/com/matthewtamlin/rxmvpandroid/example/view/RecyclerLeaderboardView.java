package com.matthewtamlin.rxmvpandroid.example.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.DiffUtil.Callback;
import android.support.v7.util.DiffUtil.DiffResult;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.common.base.Optional;
import com.matthewtamlin.rxmvpandroid.librarytestharnesses.R;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;

/**
 * A LeaderboardView that uses a RecyclerView for efficient view management.
 */
public class RecyclerLeaderboardView extends RecyclerView implements LeaderboardView {
  private final PublishSubject<PlayerViewModel> deleteRequests = PublishSubject.create();

  private final List<PlayerViewModel> players = new ArrayList<>();

  private final Adapter adapter = new Adapter();

  public RecyclerLeaderboardView(final Context context) {
    super(context);
    init();
  }

  public RecyclerLeaderboardView(final Context context, @Nullable final AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public RecyclerLeaderboardView(final Context context, @Nullable final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  private void init() {
    setAdapter(adapter);
    setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
  }

  @Override
  public Completable setPlayers(final List<PlayerViewModel> players) {
    return calculateDiff(this.players, players).flatMapCompletable(diff -> updateData(players, diff));
  }

  @Override
  public Observable<PlayerViewModel> observeDeleteRequests() {
    return deleteRequests;
  }

  @NonNull
  @Override
  public Observable<Optional<Completable>> observePendingBackActions() {
    return Observable.never(); // Doesn't handle back presses
  }

  @NonNull
  @Override
  public View asView() {
    return this;
  }

  private Single<DiffResult> calculateDiff(
      final List<PlayerViewModel> oldPlayers,
      final List<PlayerViewModel> newPlayers) {

    return Single.fromCallable(() -> DiffUtil.calculateDiff(new Callback() {
      @Override
      public int getOldListSize() {
        return oldPlayers.size();
      }

      @Override
      public int getNewListSize() {
        return newPlayers.size();
      }

      @Override
      public boolean areItemsTheSame(final int oldItemPosition, final int newItemPosition) {
        return areContentsTheSame(oldItemPosition, newItemPosition);
      }

      @Override
      public boolean areContentsTheSame(final int oldItemPosition, final int newItemPosition) {
        return oldPlayers.get(oldItemPosition).equals(newPlayers.get(newItemPosition));
      }
    }));
  }

  private Completable updateData(final List<PlayerViewModel> newPlayers, final DiffResult diff) {
    return Completable.fromRunnable(() -> {
      players.clear();
      players.addAll(newPlayers);

      diff.dispatchUpdatesTo(adapter);
    });
  }

  private class ViewHolder extends RecyclerView.ViewHolder {
    public final TextView nameLabel;

    public final TextView scoreLabel;

    public final ImageButton deleteButton;

    public ViewHolder(final View itemView) {
      super(itemView);

      nameLabel = itemView.findViewById(R.id.name);
      scoreLabel = itemView.findViewById(R.id.score);
      deleteButton = itemView.findViewById(R.id.delete);
    }
  }

  private class Adapter extends RecyclerView.Adapter<ViewHolder> {
    private LayoutInflater layoutInflater;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
      return new ViewHolder(getInflater().inflate(R.layout.recycler_leaderboard_view_item, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
      final PlayerViewModel player = players.get(position);

      holder.nameLabel.setText(player.getName());
      holder.scoreLabel.setText(player.getHighscore());
      holder.deleteButton.setOnClickListener(view -> {
        final PlayerViewModel boundPlayer = players.get(holder.getAdapterPosition());
        deleteRequests.onNext(boundPlayer);
      });
    }

    @Override
    public int getItemCount() {
      return players.size();
    }

    private LayoutInflater getInflater() {
      if (layoutInflater == null) {
        layoutInflater = LayoutInflater.from(getContext());
      }

      return layoutInflater;
    }
  }
}