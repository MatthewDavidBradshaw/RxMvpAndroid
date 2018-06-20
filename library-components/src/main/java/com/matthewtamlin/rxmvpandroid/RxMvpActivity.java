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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * An activity for using the RxMvpAndroid architecture. The activity hosts an {@link RxMvpView} and an
 * {@link RxMvpPresenter}, and manages presentation task subscription/disposal.
 * <p>
 * To use this activity, implement {@link #getView()} and {@link #getPresenter()}. The activity adds the view to
 * the hierarchy in {@link #onCreate(Bundle)}, so it is important that subclasses do not change the content view.
 * Whenever {@link #onResume()} is called, the activity gets a new presentation task from the presenter and subscribes
 * to it. The subscription continues until the presentation task completes, or until the {@link #onPause()} callback is
 * delivered.
 * <p>
 * When the user presses the back button, the activity attempts to execute the pending back action of the view or the
 * presenter. If both have pending back actions, then the view receives priority. If neither have pending back actions,
 * then the standard back press behaviour applies. The pending back actions of the view and the presenter are
 * always ignored while the activity is not in a resumed state.
 * <p>
 * The {@link RxMvpActivityDelegate} is provided as an alternative to this activity. It can be used to achieve the
 * RxMvpAndroid architecture in activities that do not extend from this class.
 *
 * @param <V>
 *     the type of view
 * @param <P>
 *     the type of presenter
 */
public abstract class RxMvpActivity<V extends RxMvpView, P extends RxMvpPresenter> extends AppCompatActivity {
  private RxMvpActivityDelegate<V, P> delegate;

  /**
   * Called from {@link #onCreate(Bundle)} to get the view for this activity. Each call must return the same instance.
   *
   * @return the view to display
   */
  @NonNull
  public abstract V getView();

  /**
   * Called from {@link #onCreate(Bundle)} to get the presenter for this activity. Each call must return the same
   * instance.
   *
   * @return the presenter to use
   */
  @NonNull
  public abstract P getPresenter();

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    delegate = new RxMvpActivityDelegate<>(getView(), getPresenter());
  }

  @Override
  protected void onResume() {
    super.onResume();

    delegate.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();

    delegate.onPause();
  }

  @Override
  public void onBackPressed() {
    final boolean handledByDelegate = delegate.onBackPressed();

    if (!handledByDelegate) {
      super.onBackPressed();
    }
  }
}