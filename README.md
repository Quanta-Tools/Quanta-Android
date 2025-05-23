# Quanta Android SDK

This is the official Android SDK for Quanta.tools.

## Installation

### 1. Add the SDK to your project

**Option A: From Maven Central (Recommended for production)**

Add the following to your module-level `build.gradle.kts` file and use the latest version:

![latest version shield](https://img.shields.io/github/v/release/Quanta-Tools/Quanta-Android?style=flat-square)

```gradle
dependencies {
    // For VERSION, see number above, NO `v` prefix, e.g. `1.0.0`
    implementation("tools.quanta:sdk:VERSION")
}
```

And ensure you have `mavenCentral()` in your project-level `settings.gradle.kts` or `build.gradle.kts` repositories block:

```gradle
// settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
```

**Option B: Link locally for development/testing**

If you have the SDK checked out locally and want to test changes before publishing:

1.  Ensure your SDK project (`quanta-android`) and your app project are in the same parent directory or adjust the path accordingly.
2.  In your app project's `settings.gradle.kts` file, add:

    ```gradle
    includeBuild("../quanta-android") // Adjust path if necessary
    ```

3.  In your app module's `build.gradle.kts` file, add the dependency:

    ```gradle
    dependencies {
        implementation(project(":sdk")) // Assumes the artifactId is 'sdk'
    }
    ```

### 2. Configure your QuantaAppId and other SDK settings

The SDK requires a `QuantaAppId` and can be configured with other settings directly in your app's `AndroidManifest.xml` file.

1.  Open your app module's `AndroidManifest.xml` file.
2.  Inside the `<application>` tag, add `<meta-data>` elements for your `QuantaAppId` and any other SDK configurations.

    For example:

    ```xml
    <application
        android:name=".MyApplication"
        android:label="@string/app_name"
        android:icon="@mipmap/ic_launcher">

        <!-- Other application elements -->

        <!-- Quanta SDK Configuration -->
        <meta-data
            android:name="tools.quanta.AppId"
            android:value="YOUR_APP_ID_HERE" />
        <meta-data
            android:name="tools.quanta.LogInProd"
            android:value="true" /> <!-- Example: Enable logging in production -->
        <meta-data
            android:name="tools.quanta.LogInDebug"
            android:value="true" /> <!-- Example: Enable logging in debug -->
        <!-- Add other Quanta SDK meta-data tags as needed -->

    </application>
    ```

    Ensure you replace `"YOUR_APP_ID_HERE"` with your actual Quanta Application ID. The `ConfigReader` component of the SDK will automatically pick up these values.

## Basic Usage

(Further instructions on how to initialize and use the SDK will be added here.)

## Contributing

(Details on how to contribute to the SDK will be added here.)
