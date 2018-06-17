/*
 * Copyright 2018 Matthew David Tamlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.matthewtamlin.rxmvpandroid;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.common.base.Optional;

import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Handles the interface between reactive MVP components and an Android activity. To use this delegate in an activity,
 * create an instance during {@link Activity#onCreate(Bundle)} and delegate onResume(), onPause() and onBackPressed()
 * calls.
 * <p>
 * The delegate handles stream subscription and disposal, but does not establish communication between the view and
 * the presenter. This must be achieved externally by injecting the view into the presenter or by some other means.
 * <p>
 * The view is given priority When handling back presses. If the view fails to handle a back press, then the presenter
 * is given the opportunity.
 *
 * @param <V>
 *     the type of view displayed in the activity
 * @param <P>
 *     the type of presenter used in the activity
 */
public class
RxMvpActivityDelegate<V extends RxMvpView, T extends RxMvpPresentation, P extends RxMvpPresenter<T>> {
  private final V view;

  private final P presenter;

  private T currentPresentation;

  private Disposable currentTasks;

  private Completable pendingViewBackAction;

  private Completable pendingPresentationBackAction;

  /**
   * Constructs a new RxMvpActivityDelegate.
   *
   * @param view
   *     the view displayed in the activity
   * @param presenter
   *     the presenter used in the activity
   */
  public RxMvpActivityDelegate(@NonNull final V view, @NonNull final P presenter) {
    this.view = checkNotNull(view);
    this.presenter = checkNotNull(presenter);
  }

  /**
   * Creates and starts a new presentation. Delegate all {@link Activity#onResume()} calls from the host activity.
   *
   * @throws IllegalStateException
   *     if already in a resumed state
   */
  public void onResume() {
    if (currentPresentation != null || currentTasks != null) {
      throw new IllegalStateException("Attempted to resume from resumed state.");
    }

    currentPresentation = presenter.createPresentation();

    currentTasks = Completable
        .mergeArray(
            currentPresentation.getTasks(),
            savePendingViewBackActions(),
            savePendingPresentationBackActions())
        .subscribe();
  }

  /**
   * Cancels the existing presentation. Safe to call if a presentation is not currently in progress. Delegate all
   * {@link Activity#onPause()} calls from the host activity.
   */
  public void onPause() {
    if (currentPresentation != null) {
      currentPresentation = null;
    }

    if (currentTasks != null) {
      currentTasks.dispose();
      currentTasks = null;
    }
  }

  /**
   * Attempts to handle a back press. The view receives priority over the current presentation. Delegate all
   * {@link Activity#onBackPressed()} calls from the host activity.
   *
   * @return true if the back press was handled, false otherwise
   */
  public boolean onBackPressed() {
    if (currentPresentation == null) {
      // Back actions are only valid while a presentation is in progress
      return false;
    }

    // Make a copy to avoid interference
    final Completable pendingViewBackAction = this.pendingViewBackAction;
    final Completable pendingPresentationBackAction = this.pendingPresentationBackAction;

    if (pendingViewBackAction != null) {
      this.pendingViewBackAction = null; // Used -> no longer needed
      pendingViewBackAction.blockingAwait();
      return true;

    } else if (pendingPresentationBackAction != null) {
      this.pendingPresentationBackAction = null; // Used -> no longer needed
      pendingPresentationBackAction.blockingAwait();
      return true;

    } else {
      return false;
    }
  }

  /**
   * @return the current presentation if any
   */
  @NonNull
  public Optional<T> getCurrentPresentation() {
    return Optional.fromNullable(currentPresentation);
  }

  @NonNull
  private Completable savePendingViewBackActions() {
    return view
        .observePendingBackActions()
        .flatMapCompletable(optionalAction ->
            Completable.fromRunnable(() -> pendingViewBackAction = optionalAction.orNull()));
  }

  @NonNull
  private Completable savePendingPresentationBackActions() {
    return currentPresentation
        .observePendingBackActions()
        .flatMapCompletable(action ->
            Completable.fromRunnable(() -> pendingPresentationBackAction = action.orNull()));
  }
}