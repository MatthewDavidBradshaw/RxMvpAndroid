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

import io.reactivex.Completable;

/**
 * The presenter in an MVP architecture. The presenter is responsible for:
 * <ul>
 * <li>Responding to UI layer events.</li>
 * <li>Responding to data layer events.</li>
 * <li>Pushing updates to the UI layer.</li>
 * <li>Pushing updates to the data layer.</li>
 * </ul>
 * <p>
 * The presenter expresses the ongoing presentation tasks as a completable.
 */
public interface RxMvpPresenter extends BackHandler {
  /**
   * Creates a completable that performs the ongoing presentation tasks.
   *
   * @return a new completable that performs the presentation tasks
   */
  @NonNull
  public Completable createTasks();
}