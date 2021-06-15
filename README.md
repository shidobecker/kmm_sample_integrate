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
