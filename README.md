# Quanta Android SDK

This is the official Android SDK for Quanta.tools.

## Installation

### 1. Add the SDK to your project

**Option A: From Maven Central (Recommended for production)**

Add the following to your module-level `build.gradle.kts` file:

```gradle
dependencies {
    implementation("tools.quanta:sdk:LATEST_VERSION") // Replace LATEST_VERSION with the actual latest version
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

### 2. Configure your QuantaAppId

The SDK requires a `QuantaAppId` to associate analytics data with your application.

1.  Create an XML file named `quanta_config.xml` (or any other name you prefer) in your app's `res/xml/` directory. If the directory doesn't exist, create it.

2.  Add your `QuantaAppId` to this file:

    ```xml
    <?xml version="1.0" encoding="utf-8"?>
    <config>
        <string name="QuantaAppId">YOUR_APP_ID_HERE</string>
        <!-- You can add other SDK configuration values here as needed -->
    </config>
    ```

3.  When initializing the SDK or its components that require configuration (like `ConfigReader`), you will pass the resource ID of this XML file. For example:

    ```kotlin
    // In your Application class or relevant setup location
    // import tools.quanta.sdk.config.ConfigReader
    // import your.app.R // Import your app's R file

    // val configReader = ConfigReader(applicationContext, R.xml.quanta_config)
    // val appId = configReader.getString("QuantaAppId")
    ```

## Basic Usage

(Further instructions on how to initialize and use the SDK will be added here.)

## Contributing

(Details on how to contribute to the SDK will be added here.)
