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
import android.support.annotation.NonNull;

import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Delegate for using the RxMvpAndroid architecture in activities that don't extend from {@link RxMvpActivity}. To use
 * this delegate in an activity, create an instance and pass all onResume(), onPause() and onBackPressed() calls
 * from the activity to the respective delegate methods.
 * <p>
 * The delegate does not handle interaction between the view and the presenter, it merely handles back presses and
 * manages subscription/disposal of the presentation task. View-presenter interaction must be established externally by
 * injection, configuration, or some other means.
 *
 * @param <V>
 *     the type of view
 * @param <P>
 *     the type of presenter
 */
public class RxMvpActivityDelegate<V extends RxMvpView, P extends RxMvpPresenter> {
  private final V view;

  private final P presenter;

  private Disposable currentTasks;

  private Completable pendingViewBackAction;

  private Completable pendingPresentationBackAction;

  /**
   * Constructs a new RxMvpActivityDelegate.
   *
   * @param view
   *     the view
   * @param presenter
   *     the presenter
   */
  public RxMvpActivityDelegate(@NonNull final V view, @NonNull final P presenter) {
    this.view = checkNotNull(view);
    this.presenter = checkNotNull(presenter);
  }

  /**
   * Resumes the presentation by getting a new presentation task from the presenter and subscribing to it.
   * <p>
   * Delegate all {@link Activity#onResume()} calls from the host activity to this method.
   *
   * @throws IllegalStateException
   *     if already resumed
   */
  public void onResume() {
    if (currentTasks != null) {
      throw new IllegalStateException("Attempted to resume from resumed state.");
    }

    currentTasks = Completable
        .mergeArray(
            presenter.createOngoingPresentationTasks(),
            savePendingViewBackActions(),
            savePendingPresentationBackActions())
        .subscribe();
  }

  /**
   * Pauses the presentation by disposing of the existing presentation task.
   * <p>
   * Delegate all {@link Activity#onPause()} calls from the host activity to this method.
   */
  public void onPause() {
    if (currentTasks != null) {
      currentTasks.dispose();
      currentTasks = null;
    }
  }

  /**
   * Attempts to handle a back press. If both the view and the presenter have pending back actions, the view receives
   * priority. If neither has a pending back action, the back press is not handled. Back presses are never handled
   * while paused.
   * <p>
   * Delegate all {@link Activity#onBackPressed()} calls from the host activity to this method.
   *
   * @return true if the back press was handled, false otherwise
   */
  public boolean onBackPressed() {
    if (currentTasks == null) {
      // Back actions can only be consumed while a presentation task is in progress
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

  @NonNull
  private Completable savePendingViewBackActions() {
    return view
        .observePendingBackActions()
        .flatMapCompletable(optionalAction ->
            Completable.fromRunnable(() -> pendingViewBackAction = optionalAction.orNull()));
  }

  @NonNull
  private Completable savePendingPresentationBackActions() {
    return presenter
        .observePendingBackActions()
        .flatMapCompletable(optionalAction ->
            Completable.fromRunnable(() -> pendingPresentationBackAction = optionalAction.orNull()));
  }
}