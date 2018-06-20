# RxMvpAndroid
A framework for implementing MVP reactively in Android projects.

This framework is very lightweight because it was designed to guide developers rather than get in the way. It provides the glue for embedding reactive MVP components in Android activities, but leaves you free to implement the components however you like. Essentially the framework performs two functions:
- Interfacing a reactive back stack with the Android back press callback.
- Interfacing a reactive presentation with the Android lifecycle.

The framework handles these events in a way that is compatible and consistent with best practices for reactive programming.