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
import android.widget.FrameLayout;

/**
 * An activity that hosts an {@link RxMvpView} and an {@link RxMvpPresenter}.
 * <p>
 * The activity handles stream subscription and disposal, but does not establish communication between the view and
 * the presenter.
 * <p>
 * The view is given priority When handling back presses. If the view fails to handle a back press, then the presenter
 * is given the opportunity. If both fail, the standard back press behaviour applies.
 *
 * @param <V>
 *     the type of view
 * @param <P>
 *     the type of presenter
 */
public abstract class RxMvpActivity<V extends RxMvpView, P extends RxMvpPresenter>
    extends AppCompatActivity {

  private RxMvpActivityDelegate<V, P> delegate;

  private FrameLayout rootView;

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

    setContentView(R.layout.rx_mvp_activity);

    rootView = findViewById(R.id.root);
    rootView.addView(getView().asView());

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

  /**
   * Gets the root view of this activity. The view supplied by {@link #getView()} is a direct child of this view.
   *
   * @return the root view of this activity, or null if {@link #onCreate(Bundle)} has not been called yet
   */
  @NonNull
  public FrameLayout getRootView() {
    return rootView;
  }
}