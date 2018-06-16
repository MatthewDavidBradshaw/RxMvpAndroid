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

import io.reactivex.Completable;

/**
 * Performs presentation tasks and handles back presses. Presentations should be stateless, and operate entirely by
 * responding to events from the view and the data source.
 */
public interface Presentation extends BackHandler {
  /**
   * Gets a completable that responds to changes in the UI and the data source, and pushes updates as required. The
   * same instance is always returned, so external care should be taken to ensure the completable is used
   * appropriately and not reused.
   *
   * @return a completable that performs the presentation tasks
   */
  public Completable getTasks();
}