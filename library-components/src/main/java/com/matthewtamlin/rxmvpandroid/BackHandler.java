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

import android.support.annotation.NonNull;

import com.google.common.base.Optional;

import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * Something capable of consuming back button presses.
 */
public interface BackHandler {
  /**
   * Gets an observable stream of pending back actions. The back actions are emitted as completables wrapped in
   * optionals. If no back action is currently available, an empty optional is emitted. At any point in time, only the
   * most recent emission is relevant and all prior emissions should be ignored.
   *
   * @return a new observable that emits pending back actions
   */
  @NonNull
  public Observable<Optional<Completable>> observePendingBackActions();
}