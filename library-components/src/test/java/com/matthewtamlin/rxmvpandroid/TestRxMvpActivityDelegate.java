package com.matthewtamlin.rxmvpandroid;

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
  private DataSource dataSource;

  private TestView view;

  private RxMvpActivityDelegate<TestView, TestPresentation, TestPresenter> delegate;

  @SuppressWarnings("unchecked")
  @Before
  public void setup() {
    dataSource = mock(DataSource.class);
    view = new TestView();

    final TestPresenter presenter = new TestPresenter(view, dataSource);

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
  public void testStreamBehaviour_beforeOnResume() {
    verify(dataSource, never()).saveLabel(any());
  }

  @Test
  public void testStreamBehaviour_afterOnResumeButBeforeOnPause() {
    delegate.onResume();

    view.label.onNext("test");

    verify(dataSource, times(1)).saveLabel("test");
  }

  @Test
  public void testStreamBehaviour_afterOnPause() {
    delegate.onResume();
    delegate.onPause();

    view.label.onNext("test");

    verify(dataSource, never()).saveLabel(any());
  }

  @Test
  public void testOnBackPressed_beforeOnResume_noPendingBackActionsEmitted() {
    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(false));
  }

  @Test
  public void testOnBackPressed_beforeOnResume_absentViewBackActionEmitted() {
    view
        .pendingBackActions
        .onNext(Optional.absent());

    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(false));
  }

  @Test
  public void testOnBackPressed_beforeOnResume_viewBackActionEmitted() {
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
  public void testOnBackPressed_afterOnResumeButBeforeOnPause_noPendingBackActionsEmitted() {
    delegate.onResume();

    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(false));
  }

  @Test
  public void testOnBackPressed_afterOnResumeButBeforeOnPause_absentViewBackActionEmitted() {
    delegate.onResume();

    view
        .pendingBackActions
        .onNext(Optional.absent());

    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(false));
  }

  @Test
  public void testOnBackPressed_afterOnResumeButBeforeOnPause_absentPresentationBackActionEmitted() {
    delegate.onResume();

    delegate
        .getCurrentPresentation()
        .get()
        .pendingBackActions
        .onNext(Optional.absent());

    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(false));
  }

  @Test
  public void testOnBackPressed_afterOnResumeButBeforeOnPause_absentViewAndPresentationBackActionsEmitted() {
    delegate.onResume();

    view
        .pendingBackActions
        .onNext(Optional.absent());

    delegate
        .getCurrentPresentation()
        .get()
        .pendingBackActions
        .onNext(Optional.absent());

    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(false));
  }

  @Test
  public void testOnBackPressed_afterOnResumeButBeforeOnPause_viewBackActionEmitted() {
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
  public void testOnBackPressed_afterOnResumeButBeforeOnPause_presentationBackActionEmitted() {
    delegate.onResume();

    final AtomicBoolean backActionExecuted = new AtomicBoolean(false);
    final Completable backAction = Completable.fromRunnable(() -> backActionExecuted.set(true));

    delegate
        .getCurrentPresentation()
        .get()
        .pendingBackActions
        .onNext(Optional.of(backAction));

    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(true));
    assertThat(backActionExecuted.get(), is(true));
  }

  @Test
  public void testOnBackPressed_afterOnResumeButBeforeOnPause_viewAndPresentationBackActionsEmitted() {
    delegate.onResume();

    final AtomicBoolean viewBackActionExecuted = new AtomicBoolean(false);
    final Completable viewBackAction = Completable.fromRunnable(() -> viewBackActionExecuted.set(true));

    final AtomicBoolean presenterBackActionExecuted = new AtomicBoolean(false);
    final Completable presenterBackAction = Completable.fromRunnable(() -> presenterBackActionExecuted.set(true));

    view
        .pendingBackActions
        .onNext(Optional.of(viewBackAction));

    delegate
        .getCurrentPresentation()
        .get()
        .pendingBackActions
        .onNext(Optional.of(presenterBackAction));

    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(true));
    assertThat(viewBackActionExecuted.get(), is(true));
    assertThat(presenterBackActionExecuted.get(), is(false));
  }

  @Test
  public void testOnBackPressed_afterOnPause_noPendingBackActionsEmitted() {
    delegate.onResume();
    delegate.onPause();

    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(false));
  }

  @Test
  public void testOnBackPressed_afterOnPause_absentViewBackActionEmitted() {
    delegate.onResume();
    delegate.onPause();

    view
        .pendingBackActions
        .onNext(Optional.absent());

    final boolean handledByDelegate = delegate.onBackPressed();

    assertThat(handledByDelegate, is(false));
  }

  @Test
  public void testOnBackPressed_afterOnPause_viewBackActionEmitted() {
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
  public void testOnBackPressedTwice_presentationBackActionEmitted() {
    delegate.onResume();

    final AtomicInteger backActionExecutedCount = new AtomicInteger(0);
    final Completable backAction = Completable.fromRunnable(backActionExecutedCount::incrementAndGet);

    delegate
        .getCurrentPresentation()
        .get()
        .pendingBackActions
        .onNext(Optional.of(backAction));

    final boolean firstPressHandledByDelegate = delegate.onBackPressed();
    final boolean secondPressHandledByDelegate = delegate.onBackPressed();

    assertThat(firstPressHandledByDelegate, is(true));
    assertThat(secondPressHandledByDelegate, is(false));
    assertThat(backActionExecutedCount.get(), is(1));
  }

  @Test
  public void testOnBackPressedTwice_viewAndPresentationBackActionEmitted() {
    delegate.onResume();

    final AtomicInteger viewBackActionExecutedCount = new AtomicInteger(0);
    final Completable viewBackAction = Completable.fromRunnable(viewBackActionExecutedCount::incrementAndGet);

    final AtomicInteger presentationBackActionExecutedCount = new AtomicInteger(0);
    final Completable presentationBackAction = Completable.fromRunnable(
        presentationBackActionExecutedCount::incrementAndGet);

    view
        .pendingBackActions
        .onNext(Optional.of(viewBackAction));

    delegate
        .getCurrentPresentation()
        .get()
        .pendingBackActions
        .onNext(Optional.of(presentationBackAction));

    final boolean firstPressHandledByDelegate = delegate.onBackPressed();
    final boolean secondPressHandledByDelegate = delegate.onBackPressed();

    assertThat(firstPressHandledByDelegate, is(true));
    assertThat(secondPressHandledByDelegate, is(true));
    assertThat(viewBackActionExecutedCount.get(), is(1));
    assertThat(presentationBackActionExecutedCount.get(), is(1));
  }

  static interface DataSource {
    public void saveLabel(final String label);
  }

  static class TestView implements RxMvpView {
    public final PublishSubject<Optional<Completable>> pendingBackActions = PublishSubject.create();

    public final PublishSubject<String> label = PublishSubject.create();

    @Override
    public Observable<Optional<Completable>> observePendingBackActions() {
      return pendingBackActions;
    }

    @Override
    public View asView() {
      return mock(View.class);
    }

    public Observable<String> observeLabel() {
      return label;
    }
  }

  static class TestPresentation implements RxMvpPresentation {
    public final PublishSubject<Optional<Completable>> pendingBackActions = PublishSubject.create();

    private final TestView view;

    private final DataSource dataSource;

    public TestPresentation(final TestView view, final DataSource dataSource) {
      this.view = view;
      this.dataSource = dataSource;
    }

    @Override
    public Completable getTasks() {
      return view
          .observeLabel()
          .flatMapCompletable(label -> Completable.fromRunnable(() -> dataSource.saveLabel(label)));
    }

    @Override
    public Observable<Optional<Completable>> observePendingBackActions() {
      return pendingBackActions;
    }
  }

  static class TestPresenter implements RxMvpPresenter<TestPresentation> {
    private final TestView view;

    private final DataSource dataSource;

    public TestPresenter(final TestView view, final DataSource dataSource) {
      this.view = view;
      this.dataSource = dataSource;
    }

    @Override
    public TestPresentation createPresentation() {
      return new TestPresentation(view, dataSource);
    }
  }
}