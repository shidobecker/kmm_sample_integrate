# Simple login

This is a sample project for the ["Make your Android application work on iOS"](https://kotlinlang.org/docs/mobile/integrate-in-existing-app.html) tutorial. Master branch contains the project's initial state: it's a simple Android application generated with the Android Studio "Login application" wizard. You could find the final state with the iOS application and shared KMM module in the [final branch](https://github.com/Kotlin/kmm-integration-sample/tree/final)

## Start Project Clone:
https://github.com/Kotlin/kmm-integration-sample

# Make your code cross-platform 
To have a KMM application that works on iOS, you'll first make your code cross-platform, and then you’ll reuse your cross-platform code in a new iOS application.

- To make your code cross-platform:

- Decide what code to make cross-platform.

- Create a shared module for cross-platform code.

- Add a dependency on the shared module to your Android application.

- Make the business logic cross-platform.

- Run your cross-platform application on Android.

## Decide what code to make cross-platform

Decide which code of your Android application is better to share for iOS and which to keep native. *A simple rule is: share what you want to reuse as much as possible.* The business logic is often the same for both Android and iOS, so it's a great candidate for reuse. Here you can get more recommendations on making code cross-platform. [Architect KMM app](https://kotlinlang.org/docs/mobile/architect-kmm-app.html)

In your sample Android application, the business logic is stored in the package com.jetbrains.simplelogin.androidapp.data. Your future iOS application will use the same logic, so you should make it cross-platform, as well.

### Create a shared module for cross-platform code
The cross-platform code that is used for both iOS and Android is stored in the shared module. KMM provides a special wizard for creating such modules.

In your Android project, create a KMM shared module for your cross-platform code. Later you'll connect it to your existing Android application and your future iOS application.

In Android Studio, click File | New | New Module.

In the list of templates, select KMM Shared Module, enter the module name shared, and select the Xcode build phases (packForXcode task) in the list of iOS framework distribution options.
This is required for connecting the shared module to the iOS application.

KMM shared module
Click Finish.

The wizard will create the KMM shared module, update the configuration files, and create files with classes that demonstrate the benefits of Kotlin Multiplatform. You can learn more about the KMM project structure.[KMM Project Structure](https://kotlinlang.org/docs/mobile/discover-kmm-project.html)

### Issues on this step:

Configuration with name 'testApi' not found. [YoutTrack](https://youtrack.jetbrains.com/issue/KT-43944)
Add this before kotlin block
``` kotlin
android {
    configurations {
        create("androidTestApi")
        create("androidTestDebugApi")
        create("androidTestReleaseApi")
        create("testApi")
        create("testDebugApi")
        create("testReleaseApi")
    }
}
```


## Add a dependency on the shared module to your Android application
To use cross-platform code in your Android application, connect the shared module to it, move the business logic code there, and make this code cross-platform.

Ensure that compileSdkVersion and minSdkVersion in build.gradle.kts of the shared module are the same as those in the build.gradle of your Android application in the app module.
If they are different, update them in the build.gradle.kts of the shared module. Otherwise, you'll encounter a compile error.

Add a dependency on the shared module to the build.gradle of your Android application.

dependencies {
    implementation project(':shared')
}
 
Synchronize the Gradle files by clicking Sync Now in the warning.

Synchronize the Gradle files
To make sure that the shared module is successfully connected to your application, dump the greeting() function result to the log by updating the onCreate() method of the LoginActivity class.
```
override fun onCreate(savedInstanceState: Bundle?) {
   super.onCreate(savedInstanceState)

   Log.i("Login Activity", "Hello from shared module: " + (Greeting().greeting()))

}
 ```
Search for Hello in the log, and you'll find the greeting from the shared module.


## Make the business logic cross-platform
You can now extract the business logic code to the KMM shared module and make it platform-independent. This is necessary for reusing the code for both Android and iOS.

Move the business logic code com.jetbrains.simplelogin.androidapp.data from the app directory to the com.jetbrains.simplelogin.shared package in the shared/src/commonMain directory. You can drag and drop the package or refactor it by moving everything from one directory to another.

Drag and drop the package with the business logic code
When Android Studio asks what you'd like to do, select to move the package, and then approve the refactoring.

Refactor the business logic package
Ignore all warnings about platform-dependent code and click Continue.

Warnings about platform-dependent code
Remove Android-specific code by replacing it with cross-platform Kotlin code or connecting to Android-specific APIs using expect and actual declarations. See the following sections for details.


###Replace Android-specific code with cross-platform cod 
To make your code work well on both Android and iOS, replace all JVM dependencies with Kotlin dependencies wherever possible.

In the login() function of the LoginDataSource class, replace *IOException*, which is not available in Kotlin, with *RuntimeException*.

// Before
`return Result.Error(IOException("Error logging in", e))`
 
//After
`return Result.Error(RuntimeException("Error logging in", e))`
 
For email validation, replace the Patterns class from the android.utils package with a Kotlin regular expression matching the pattern in the LoginDataValidator class:

// Before
`private fun isEmailValid(email: String) = Patterns.EMAIL_ADDRESS.matcher(email).matches()`

//After
```
private fun isEmailValid(email: String) = emailRegex.matches(email)

companion object {
   private val emailRegex =
           ("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                   "\\@" +
                   "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                   "(" +
                   "\\." +
                   "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                   ")+").toRegex()
}
```



### Connect to platform-specific APIs from the cross-platform code
A universally unique identifier (UUID) for fakeUser in LoginDataSource is generated using the java.util.UUID class, which is not available for iOS.

`val fakeUser = LoggedInUser(java.util.UUID.randomUUID().toString(), "Jane Doe")`
 
Since the Kotlin standard library doesn't provide functionality for generating UUIDs, you still need to use platform-specific functionality for this case.

Provide the expect declaration for the randomUUID() function in the shared code and its actual implementations for each platform – Android and iOS – in the corresponding source sets. You can learn more about connecting to platform-specific APIs.


Create a `Utils.kt` file in the `shared/src/commonMain` directory and provide the expect declaration:
```
package com.jetbrains.simplelogin.shared

expect fun randomUUID(): String
 ```
Create a `Utils.kt` file in the `shared/src/androidMain` directory and provide the actual implementation for randomUUID() in Android:
```
package com.jetbrains.simplelogin.shared

import java.util.*
actual fun randomUUID() = UUID.randomUUID().toString()
 ```
Create a `Utils.kt` file in the `shared/src/iosMain` directory and provide the actual implementation for randomUUID() in iOS:
```
package com.jetbrains.simplelogin.shared

import platform.Foundation.NSUUID
actual fun randomUUID(): String = NSUUID().UUIDString()
```


## Make your cross-platform application work on iOS 
Once you've made your Android application cross-platform, you can create an iOS application and reuse the shared business logic in it.

- Create an iOS project in Xcode.

- Compile the shared module into a framework for the iOS project.

- Connect the framework to your iOS project.

- Automate iOS project updates.

- Connect the shared module to the iOS project.

### Create an iOS project in Xcode 
In Xcode, click File | New | Project.

Select a template for an iOS app and click Next.

iOS project template
As the product name, specify simpleLoginIOS and click Next.

iOS project settings
As the location for your project, select the directory that stores your cross-platform application, for example, kmm-integrate-into-existing-app.


You can rename the simpleLoginIOS directory to iosApp for consistency with other top-level directories of your cross-platform project.

Renamed iOS project directory in Android Studio - *You can rename, but don't, Xcode might delete project files *

##Compile the shared module into a framework for the iOS project
To use Kotlin code in your iOS project, compile shared code into a .framework.

In Android Studio, run the packForXcode Gradle task in the Terminal:

`./gradlew packForXcode`
 
You can also run the packForXcode Gradle task by double-clicking it in the list of Gradle tasks.

The generated framework is stored in the shared/build/xcode-frameworks/ directory.

### Issues on this step:

*To take advantage of the new functionality for Cocoapods Integration like synchronizing with the Xcode project 
and supporting dependencies on pods, please install the `cocoapods-generate` plugin for CocoaPods 
by calling `gem install cocoapods-generate` in terminal.*
How to solve: `brew install cocoapods-generate`

*KotlinTarget with name 'iosX64' not found*

Changing the whole build.gradle.kts to this, seems to solve it:

``` groovy
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

android {
    configurations {
        create("androidTestApi")
        create("androidTestDebugApi")
        create("androidTestReleaseApi")
        create("testApi")
        create("testDebugApi")
        create("testReleaseApi")
    }
}


kotlin {
    android()
    ios {
        binaries {
            framework {
                baseName = "kmmsharedmodule"
            }
        }
    }
    sourceSets {
        val commonMain by getting
        val androidMain by getting
        val iosMain by getting
    }
}

android {
    compileSdkVersion(30)
    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(30)
    }
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
}

val packForXcode by tasks.creating(Sync::class) {
    group = "build"
    val mode = System.getenv("CONFIGURATION") ?: "DEBUG"
    val sdkName = System.getenv("SDK_NAME") ?: "iphonesimulator"
    val targetName = "ios" + if (sdkName.startsWith("iphoneos")) "Arm64" else "X64"
    val framework =
        kotlin.targets.getByName<KotlinNativeTarget>(targetName).binaries.getFramework(mode)
    inputs.property("mode", mode)
    dependsOn(framework.linkTask)
    val targetDir = File(buildDir, "xcode-frameworks")
    from({ framework.outputDirectory })
    into(targetDir)
}
tasks.getByName("build").dependsOn(packForXcode)
```


## Connect the framework to your iOS project 
Once you have the framework, you can connect it to your iOS project manually.

An alternative is to configure integration via Cocoapods, but that integration is beyond the scope of this tutorial.

Connect your framework to the iOS project manually.

In Xcode, open the iOS project settings by double-clicking the project name.

Click the + under Frameworks, Libraries, and Embedded Content.

Add the generated framework
Click Add Other, then click Add Files, and select the generated framework in shared/build/xcode-frameworks/shared.framework.

Framework is added
Specify the Framework Search Path under Search Paths on the Build Settings tab – $(SRCROOT)/../shared/build/xcode-frameworks.

Framework search path
Automate iOS project updates﻿
To avoid recompiling your framework manually after every change in the KMM module, configure automatic updates of the iOS project.

On the Build Phases tab of the project settings, click the + and add New Run Script Phase.

Add run script phase
Add the following script:
```
cd "$SRCROOT/.."
./gradlew :shared:packForXCode -PXCODE_CONFIGURATION=${CONFIGURATION}
```
 
Add the script
Move the Run Script phase before the Compile Sources phase.

Move the Run Script phase
Connect the shared module to the iOS project﻿
In Xcode, open the ContentView.swift file and import the shared module.
```
import shared
 ```
To check that it is properly connected, use the greeting() function from the KMM module:
```
import SwiftUI
import shared

struct ContentView: View {
    var body: some View {
        Text(Greeting().greeting())
        .padding()
    }
}
```
