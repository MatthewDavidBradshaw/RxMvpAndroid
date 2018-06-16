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

/**
 * The presenter in an MVP architecture. Presenters are responsible for preparing and creating instances of
 * {@link Presentation}, and do not actually perform the presentation tasks directly.
 *
 * @param <P>
 *     the type of presentation
 */
public interface RxMvpPresenter<P extends Presentation> {
  /**
   * @return a new presentation
   */
  public P createPresentation();
}