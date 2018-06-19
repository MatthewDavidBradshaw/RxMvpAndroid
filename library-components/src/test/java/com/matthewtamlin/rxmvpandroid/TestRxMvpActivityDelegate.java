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
import android.view.View;

import com.google.common.base.Optional;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TestRxMvpActivityDelegate {
  private TestDataSource dataSource;

  private TestView view;

  private TestPresenter presenter;

  private RxMvpActivityDelegate<TestView, TestPresenter> delegate;

  @SuppressWarnings("unchecked")
  @Before
  public void setup() {
    dataSource = mock(TestDataSource.class);
    view = new TestView();
    presenter = new TestPresenter(view, dataSource);

    delegate = new RxMvpActivityDelegate<>(view, presenter);
  }

  @Test(expected = IllegalStateException.class)
  public void testOnResume_calledTwiceContiguously() {
    delegate.onResume();
    delegate.onResume();
  }

  @Test
  public void testOnPause_calledTwiceContiguously() {
    delegate.onResume();
    delegate.onPause();
    delegate.onPause();
  }

  @Test
  public void testOnPause_calledWithoutCallingOnResume() {
    delegate.onPause();
  }

  @Test
  public void testStreamBehaviour_neverResumed() {
    verify(dataSource, never()).saveText(any());
  }

  @Test
  public void testStreamBehaviour_resumed() {
    delegate.onResume();

    view.label.onNext("test");

    verify(dataSource, times(1)).saveText("test");
  }

  @Test
  public void testStreamBehaviour_paused() {
    delegate.onResume();
    delegate.onPause();

    view.label.onNext("test");

    verify(dataSource, never()).saveText(any());
  }

  @Test
  public void testStreamBehaviour_pausedThenResumed() {
    delegate.onResume();
    delegate.onPause();
    delegate.onResume();

    view.label.onNext("test");

    verify(dataSource, times(1)).saveText("test");
  }

  @Test
  public void testOnBackPressed_neverResumed_noPendingBackActionsEmitted() {
    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(false));
  }

  @Test
  public void testOnBackPressed_neverResumed_absentViewBackActionEmitted() {
    view
        .pendingBackActions
        .onNext(Optional.absent());

    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(false));
  }

  @Test
  public void testOnBackPressed_neverResumed_absentPresenterBackActionEmitted() {
    presenter
        .pendingBackActions
        .onNext(Optional.absent());

    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(false));
  }

  @Test
  public void testOnBackPressed_neverResumed_viewBackActionEmitted() {
    final AtomicBoolean backActionExecuted = new AtomicBoolean(false);
    final Completable backAction = Completable.fromRunnable(() -> backActionExecuted.set(true));

    view
        .pendingBackActions
        .onNext(Optional.of(backAction));

    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(false));
    assertThat(backActionExecuted.get(), is(false));
  }

  @Test
  public void testOnBackPressed_neverResumed_presenterBackActionEmitted() {
    final AtomicBoolean backActionExecuted = new AtomicBoolean(false);
    final Completable backAction = Completable.fromRunnable(() -> backActionExecuted.set(true));

    presenter
        .pendingBackActions
        .onNext(Optional.of(backAction));

    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(false));
    assertThat(backActionExecuted.get(), is(false));
  }

  @Test
  public void testOnBackPressed_neverResumed_viewAndPresenterBackActionsEmitter() {
    final AtomicBoolean viewBackActionExecuted = new AtomicBoolean(false);
    final Completable viewBackAction = Completable.fromRunnable(() -> viewBackActionExecuted.set(true));

    final AtomicBoolean presenterBackActionExecuted = new AtomicBoolean(false);
    final Completable presenterBackAction = Completable.fromRunnable(() -> presenterBackActionExecuted.set(true));

    view
        .pendingBackActions
        .onNext(Optional.of(viewBackAction));

    presenter
        .pendingBackActions
        .onNext(Optional.of(presenterBackAction));

    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(false));
    assertThat(viewBackActionExecuted.get(), is(false));
    assertThat(presenterBackActionExecuted.get(), is(false));
  }

  @Test
  public void testOnBackPressed_resumed_noPendingBackActionsEmitted() {
    delegate.onResume();

    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(false));
  }

  @Test
  public void testOnBackPressed_resumed_absentViewBackActionEmitted() {
    delegate.onResume();

    view
        .pendingBackActions
        .onNext(Optional.absent());

    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(false));
  }

  @Test
  public void testOnBackPressed_resumed_absentPresenterBackActionEmitted() {
    delegate.onResume();

    presenter
        .pendingBackActions
        .onNext(Optional.absent());

    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(false));
  }

  @Test
  public void testOnBackPressed_resumed_absentViewAndPresenterBackActionsEmitted() {
    delegate.onResume();

    view
        .pendingBackActions
        .onNext(Optional.absent());

    presenter
        .pendingBackActions
        .onNext(Optional.absent());

    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(false));
  }

  @Test
  public void testOnBackPressed_resumed_viewBackActionEmitted() {
    delegate.onResume();

    final AtomicBoolean backActionExecuted = new AtomicBoolean(false);
    final Completable backAction = Completable.fromRunnable(() -> backActionExecuted.set(true));

    view
        .pendingBackActions
        .onNext(Optional.of(backAction));

    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(true));
    assertThat(backActionExecuted.get(), is(true));
  }

  @Test
  public void testOnBackPressed_resumed_presenterBackActionEmitted() {
    delegate.onResume();

    final AtomicBoolean backActionExecuted = new AtomicBoolean(false);
    final Completable backAction = Completable.fromRunnable(() -> backActionExecuted.set(true));

    presenter
        .pendingBackActions
        .onNext(Optional.of(backAction));

    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(true));
    assertThat(backActionExecuted.get(), is(true));
  }

  @Test
  public void testOnBackPressed_resumed_viewAndPresenterBackActionsEmitted() {
    delegate.onResume();

    final AtomicBoolean viewBackActionExecuted = new AtomicBoolean(false);
    final Completable viewBackAction = Completable.fromRunnable(() -> viewBackActionExecuted.set(true));

    final AtomicBoolean presenterBackActionExecuted = new AtomicBoolean(false);
    final Completable presenterBackAction = Completable.fromRunnable(() -> presenterBackActionExecuted.set(true));

    view
        .pendingBackActions
        .onNext(Optional.of(viewBackAction));

    presenter
        .pendingBackActions
        .onNext(Optional.of(presenterBackAction));

    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(true));
    assertThat(viewBackActionExecuted.get(), is(true));
    assertThat(presenterBackActionExecuted.get(), is(false));
  }

  @Test
  public void testOnBackPressed_paused_noPendingBackActionsEmitted() {
    delegate.onResume();
    delegate.onPause();

    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(false));
  }

  @Test
  public void testOnBackPressed_paused_absentViewBackActionEmitted() {
    delegate.onResume();
    delegate.onPause();

    view
        .pendingBackActions
        .onNext(Optional.absent());

    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(false));
  }

  @Test
  public void testOnBackPressed_paused_absentPresenterBackActionEmitted() {
    delegate.onResume();
    delegate.onPause();

    view
        .pendingBackActions
        .onNext(Optional.absent());

    presenter
        .pendingBackActions
        .onNext(Optional.absent());

    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(false));
  }

  @Test
  public void testOnBackPressed_paused_viewBackActionEmitted() {
    delegate.onResume();
    delegate.onPause();

    final AtomicBoolean backActionExecuted = new AtomicBoolean(false);
    final Completable backAction = Completable.fromRunnable(() -> backActionExecuted.set(true));

    view
        .pendingBackActions
        .onNext(Optional.of(backAction));

    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(false));
    assertThat(backActionExecuted.get(), is(false));
  }

  @Test
  public void testOnBackPressed_paused_presenterBackActionEmitted() {
    delegate.onResume();
    delegate.onPause();

    final AtomicBoolean backActionExecuted = new AtomicBoolean(false);
    final Completable backAction = Completable.fromRunnable(() -> backActionExecuted.set(true));

    presenter
        .pendingBackActions
        .onNext(Optional.of(backAction));

    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(false));
    assertThat(backActionExecuted.get(), is(false));
  }

  @Test
  public void testOnBackPressed_paused_viewAndPresenterBackActionsEmitted() {
    delegate.onResume();
    delegate.onPause();

    final AtomicBoolean viewBackActionExecuted = new AtomicBoolean(false);
    final Completable viewBackAction = Completable.fromRunnable(() -> viewBackActionExecuted.set(true));

    final AtomicBoolean presenterBackActionExecuted = new AtomicBoolean(false);
    final Completable presenterBackAction = Completable.fromRunnable(() -> presenterBackActionExecuted.set(true));

    view
        .pendingBackActions
        .onNext(Optional.of(viewBackAction));

    presenter
        .pendingBackActions
        .onNext(Optional.of(presenterBackAction));

    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(false));
    assertThat(viewBackActionExecuted.get(), is(false));
    assertThat(presenterBackActionExecuted.get(), is(false));
  }

  @Test
  public void testOnBackPressed_pausedThenResumed_noPendingBackActionsEmitted() {
    delegate.onResume();
    delegate.onPause();
    delegate.onResume();

    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(false));
  }

  @Test
  public void testOnBackPressed_pausedThenResumed_absentViewBackActionEmitted() {
    delegate.onResume();
    delegate.onPause();
    delegate.onResume();

    view
        .pendingBackActions
        .onNext(Optional.absent());

    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(false));
  }

  @Test
  public void testOnBackPressed_pausedThenResumed_absentPresenterBackActionEmitted() {
    delegate.onResume();
    delegate.onPause();
    delegate.onResume();

    view
        .pendingBackActions
        .onNext(Optional.absent());

    presenter
        .pendingBackActions
        .onNext(Optional.absent());

    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(false));
  }

  @Test
  public void testOnBackPressed_pausedThenResumed_viewBackActionEmitted() {
    delegate.onResume();
    delegate.onPause();
    delegate.onResume();

    final AtomicBoolean backActionExecuted = new AtomicBoolean(false);
    final Completable backAction = Completable.fromRunnable(() -> backActionExecuted.set(true));

    view
        .pendingBackActions
        .onNext(Optional.of(backAction));

    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(true));
    assertThat(backActionExecuted.get(), is(true));
  }

  @Test
  public void testOnBackPressed_pausedThenResumed_presenterBackActionEmitted() {
    delegate.onResume();
    delegate.onPause();
    delegate.onResume();

    final AtomicBoolean backActionExecuted = new AtomicBoolean(false);
    final Completable backAction = Completable.fromRunnable(() -> backActionExecuted.set(true));

    presenter
        .pendingBackActions
        .onNext(Optional.of(backAction));

    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(true));
    assertThat(backActionExecuted.get(), is(true));
  }

  @Test
  public void testOnBackPressed_pausedThenResumed_viewAndPresenterBackActionsEmitted() {
    delegate.onResume();
    delegate.onPause();
    delegate.onResume();

    final AtomicBoolean viewBackActionExecuted = new AtomicBoolean(false);
    final Completable viewBackAction = Completable.fromRunnable(() -> viewBackActionExecuted.set(true));

    final AtomicBoolean presenterBackActionExecuted = new AtomicBoolean(false);
    final Completable presenterBackAction = Completable.fromRunnable(() -> presenterBackActionExecuted.set(true));

    view
        .pendingBackActions
        .onNext(Optional.of(viewBackAction));

    presenter
        .pendingBackActions
        .onNext(Optional.of(presenterBackAction));

    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(true));
    assertThat(viewBackActionExecuted.get(), is(true));
    assertThat(presenterBackActionExecuted.get(), is(false));
  }

  @Test
  public void testOnBackPressedTwice_noPendingBackActionsEmitted() {
    delegate.onResume();

    view
        .pendingBackActions
        .onNext(Optional.absent());

    final boolean firstPressHandledByDelegate = delegate.onBackPressed();
    final boolean secondPressHandledByDelegate = delegate.onBackPressed();

    assertThat(firstPressHandledByDelegate, is(false));
    assertThat(secondPressHandledByDelegate, is(false));
  }

  @Test
  public void testOnBackPressedTwice_viewBackActionEmitted() {
    delegate.onResume();

    final AtomicInteger backActionExecutedCount = new AtomicInteger(0);
    final Completable backAction = Completable.fromRunnable(backActionExecutedCount::incrementAndGet);

    view
        .pendingBackActions
        .onNext(Optional.of(backAction));

    final boolean firstPressHandledByDelegate = delegate.onBackPressed();
    final boolean secondPressHandledByDelegate = delegate.onBackPressed();

    assertThat(firstPressHandledByDelegate, is(true));
    assertThat(secondPressHandledByDelegate, is(false));
    assertThat(backActionExecutedCount.get(), is(1));
  }

  @Test
  public void testOnBackPressedTwice_presenterBackActionEmitted() {
    delegate.onResume();

    final AtomicInteger backActionExecutedCount = new AtomicInteger(0);
    final Completable backAction = Completable.fromRunnable(backActionExecutedCount::incrementAndGet);

    presenter
        .pendingBackActions
        .onNext(Optional.of(backAction));

    final boolean firstPressHandledByDelegate = delegate.onBackPressed();
    final boolean secondPressHandledByDelegate = delegate.onBackPressed();

    assertThat(firstPressHandledByDelegate, is(true));
    assertThat(secondPressHandledByDelegate, is(false));
    assertThat(backActionExecutedCount.get(), is(1));
  }

  @Test
  public void testOnBackPressedTwice_viewAndPresenterBackActionEmitted() {
    delegate.onResume();

    final AtomicInteger viewBackActionExecutedCount = new AtomicInteger(0);
    final Completable viewBackAction = Completable.fromRunnable(viewBackActionExecutedCount::incrementAndGet);

    final AtomicInteger presenterBackActionExecutedCount = new AtomicInteger(0);
    final Completable presenterBackAction = Completable.fromRunnable(
        presenterBackActionExecutedCount::incrementAndGet);

    view
        .pendingBackActions
        .onNext(Optional.of(viewBackAction));

    presenter
        .pendingBackActions
        .onNext(Optional.of(presenterBackAction));

    final boolean firstPressHandledByDelegate = delegate.onBackPressed();
    final boolean secondPressHandledByDelegate = delegate.onBackPressed();

    assertThat(firstPressHandledByDelegate, is(true));
    assertThat(secondPressHandledByDelegate, is(true));
    assertThat(viewBackActionExecutedCount.get(), is(1));
    assertThat(presenterBackActionExecutedCount.get(), is(1));
  }

  public interface TestDataSource {
    public void saveText(final String text);
  }

  public static class TestView implements RxMvpView {
    public final PublishSubject<Optional<Completable>> pendingBackActions = PublishSubject.create();

    public final PublishSubject<String> label = PublishSubject.create();

    @NonNull
    @Override
    public Observable<Optional<Completable>> observePendingBackActions() {
      return pendingBackActions;
    }

    @NonNull
    @Override
    public View asView() {
      return mock(View.class);
    }

    public Observable<String> observeLabel() {
      return label;
    }
  }

  public static class TestPresenter implements RxMvpPresenter {
    public final PublishSubject<Optional<Completable>> pendingBackActions = PublishSubject.create();

    private final TestView view;

    private final TestDataSource dataSource;

    public TestPresenter(final TestView view, final TestDataSource dataSource) {
      this.view = view;
      this.dataSource = dataSource;
    }

    @NonNull
    @Override
    public Completable createOngoingPresentationTasks() {
      return view
          .observeLabel()
          .flatMapCompletable(label -> Completable.fromRunnable(() -> dataSource.saveText(label)));
    }

    @NonNull
    @Override
    public Observable<Optional<Completable>> observePendingBackActions() {
      return pendingBackActions;
    }
  }
}