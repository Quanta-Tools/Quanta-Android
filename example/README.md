# Quanta SDK Test App

This Android application is designed to test the functionality of the Quanta SDK, particularly its logging capabilities. The app demonstrates how to initialize the SDK and how to log different types of events.

## Features

1. **Simple Event Logging**: Tests the basic event logging functionality without additional parameters.
2. **Event Logging with Arguments**: Tests logging events with custom key-value pair arguments.
3. **Event Logging with Revenue**: Tests logging events with revenue tracking and additional arguments.

## Setup

1. Clone this repository
2. Open the project in Android Studio
3. Build and run the app on an emulator or physical device

## SDK Integration Details

- The Quanta SDK (version 0.0.2) is integrated via Maven Central: `tools.quanta:sdk:0.0.2`
- The SDK is initialized in the `QuantaTestApplication` class
- Configuration is provided through both the `AndroidManifest.xml` and `quanta_config.xml`

## Testing the SDK

When you launch the app, you'll see three buttons:

1. **Log Simple Event**: Calls `Quanta.log("test_simple_event")`
2. **Log Event with Arguments**: Calls `Quanta.log("test_event_with_args", args)` with a map of arguments
3. **Log Event with Revenue**: Calls `Quanta.log("purchase_event", revenue, args)` with both revenue and arguments

After clicking any button, a status message will be displayed indicating success or failure, and details will be written to the log.

## Debugging

The app enables debug logging for the Quanta SDK, so you can see detailed information in the logcat with the tag "Quanta".
