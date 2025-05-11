# Testing the Quanta SDK

This document provides instructions on how to use this test application to validate the Quanta SDK.

## Project Configuration

The test app is configured with:

- **App ID**: test_app_id
- **Debug Logging**: Enabled
- **SDK Version**: 0.0.2

## Test Procedure

1. Open the project in Android Studio
2. Build and run the app on an emulator or physical device
3. Use the following buttons to test SDK functionality:

### Testing Log() Function

The app provides several ways to test the `log()` function:

#### Simple Event

- Click the "Log Simple Event" button
- This calls `Quanta.log("test_simple_event")`
- Verify in logcat that the event is logged successfully

#### Event with Arguments

- Click the "Log Event with Arguments" button
- This calls `Quanta.log("test_event_with_args", args)` with the following arguments:
  - "source": "button_click"
  - "screen": "main_activity"
  - "timestamp": current time

#### Event with Revenue

- Click the "Log Event with Revenue" button
- This calls `Quanta.log("purchase_event", 99.99, args)` with revenue amount and arguments:
  - "product_id": "premium_subscription"
  - "currency": "USD"
  - "is_trial": "false"

### Testing User ID

- Click the "Show User ID" button
- This calls `Quanta.getUserId()` and displays the result
- This confirms that user identification is working correctly

## Debugging

To see the events being logged:

1. Open Android Studio's Logcat
2. Filter by tag: "Quanta"
3. Look for log entries from the SDK operations

Example log entries:

```
I/Quanta: Quanta SDK initialized successfully in Application class
I/Quanta: Simple event logged
I/Quanta: Event with arguments logged: {source=button_click, screen=main_activity, timestamp=1684321234}
```

## Customization

To modify the app:

1. Change the App ID in AndroidManifest.xml or quanta_config.xml
2. Add more test functions in MainActivity.kt
3. Update UI in activity_main.xml
