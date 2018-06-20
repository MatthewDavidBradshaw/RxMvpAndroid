# RxMvpAndroid
A framework for implementing MVP reactively in Android projects.

This framework is very lightweight because it was designed to guide developers rather than get in the way. It provides the glue for embedding reactive MVP components in Android activities, but leaves you free to implement the components however you like. Essentially the framework performs two functions:
- Interfacing a reactive back stack with the Android back press callback.
- Interfacing a reactive presentation with the Android lifecycle.

The framework handles these events in a way that is compatible and consistent with best practices for reactive programming.

## Releases
To use this framework, add the following to your gradle build file:
```groovy
repositories {
  jcenter()
}

dependencies {
  implementation 'com.matthew-tamlin:rxmvpandroid:1.0.0'
}
```

Older versions are available in [the Maven repo](https://bintray.com/matthewtamlin/maven).

## Usage
Using the framework is simple if you know what all the components are for and how the framework wires them together. The framework is pretty small so there isn't much to learn.

The framework contains six components:
- [Viewable](https://github.com/MatthewTamlin/RxMvpAndroid/blob/master/library-components/src/main/java/com/matthewtamlin/rxmvpandroid/Viewable.java).
- [BackHandler](https://github.com/MatthewTamlin/RxMvpAndroid/blob/master/library-components/src/main/java/com/matthewtamlin/rxmvpandroid/BackHandler.java).
- [RxMvpView](https://github.com/MatthewTamlin/RxMvpAndroid/blob/master/library-components/src/main/java/com/matthewtamlin/rxmvpandroid/RxMvpView.java).
- [RxMvpPresenter](https://github.com/MatthewTamlin/RxMvpAndroid/blob/master/library-components/src/main/java/com/matthewtamlin/rxmvpandroid/RxMvpPresenter.java).
- [RxMvpActivity](https://github.com/MatthewTamlin/RxMvpAndroid/blob/master/library-components/src/main/java/com/matthewtamlin/rxmvpandroid/RxMvpActivity.java).
- [RxMvpActivityDelegate](https://github.com/MatthewTamlin/RxMvpAndroid/blob/master/library-components/src/main/java/com/matthewtamlin/rxmvpandroid/RxMvpActivityDelegate.java).

### Viewable
Something that can be added to the view hierarchy.

The `Viewable` interface declares a single method: `View asView()`. It should always return `this`, and is essentially just a syntactically nicer way of casting `this` to `View`.

Viewable makes it easy to design your views using interfaces without unsafe casting. You could argue that Viewable leaks implementation details, but I would argue that the practical benefits exceed the disadvantages, especially considering that all Android UI elements must extend from the View class.

### BackHandler
Something that can handle back presses.

The `BackHandler` interface declares a single method: `Observable<Optional<Completable>> observePendingBackActions()`. BackHandlers publish a stream of optional completables, where the completables are pending back actions that can be executed when the user presses back. Whenever the BackHandler changes in some way that invalidates the previous back action, a new optional completable is emitted. To show that there is currently no valid back action, an empty optional is emitted. In a well designed BackHandler, the execution of the current back action should cause to the emission of an updated back action.

Classes that contain multiple BackHandler objects can expose themselves as BackHandlers by combining the back handler streams of the contained objects. Combining streams should be easy for anyone experienced with RxJava.

### RxMvpView
The `RxMvpView` interface is the V in MVP. It extends Viewable and BackHandler, and declares no additional methods.

### RxMvpPresenter
The `RxMvpPresenter` interface is the P in MVP. It extends BackHandler and declares one additional method: `Completable getOngoingPresentionTask()`.

The presenter is responsible for four tasks:
- Responding to changes in the data layer.
- Responding to changes in the UI layer.
- Pushing changes to the data layer.
- Pushing changes to the UI layer.

These four tasks are expressed as a completable. Higher level presenters can include the completables of lower level presenters in their own presentation completables, and the acivity can subscribe to the top level presentation completable. It's important to note that the presentation completable can be disposed of at any time, therefore any important state variables should be placed in the presenter class itself.

In this framework, these four tasks are merged into the completable returned from `getOngoingPresentationTask()`. The completable could be disposed of at any time, therefore any shared presentation state should be stored in the presenter itself. 

Since the presenter is a BackHandler, it can publish pending back actions. The presenter should not touch the view's pending back actions though since the framework handles them separately.

### RxMvpActivity
The `RxMvpActivity` hosts the view and the presenter, and interfaces the reactive world with the activity lifecycle. The activity does this by:
- Creating a new presentation task in `onResume()` and subscribing.
- Disposing of the existing presentation task in `onPause()`.
- Subscribing to the current pending back action in `onBackPressed()`.

If the view and the presenter have both published pending back actions when the back press callback is delivered, the activity preferences the view. If neither have published a pending back action, then the default back behaviour applies.

### RxMvpActivityDelegate
If extending from RxMvpActivity is infeasible or otherwise undesirable, you can use the `RxMvpActivityDelegate` to achieve the same results. If you peek under the hood, you'll see that RxMvpActivity is actually just wrapping RxMvpActivityDelegate.