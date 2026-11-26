# Port Pocket for Android

Check whether a development server could bind a TCP port on your phone. Open the app to see common development ports, or enter a number and tap **Watch port** to save it. Results refresh every three seconds while the screen is active. Type digits to search, or filter by status.

**Try it:** tap **Start demo listener**. An occupied row shows the assigned port, app name and PID. Tap **Stop listener** to release it. The app also releases its listener when you leave the screen.

Each check briefly attempts a bind on 127.0.0.1. Available means the bind succeeded at that moment. Occupied means the OS reported an address conflict. Restricted covers permission failures and other errors. Results cover watched IPv4 loopback TCP ports. UDP, IPv6, sockets bound to other interfaces and system-wide process attribution are outside this view. A bind test can briefly compete with another app trying to start a listener.

## Build and install

Open this directory in Android Studio with Android SDK 35, JDK 17 and Gradle 8.9. Select an Android 8.0 or newer device and press Run. With Gradle installed, run `gradle assembleDebug`. The debug APK is under `build/outputs/apk/debug/`. The repository's Port Pocket workflow also builds a downloadable debug APK.

Android restricts access to [/proc/net](https://developer.android.com/about/versions/10/privacy/changes#proc-net-filesystem) and [other apps' processes](https://developer.android.com/about/versions/14/behavior-changes-all#kill-background-processes). Port Pocket displays ownership for its own listener. All checks run locally. No account, API service or root access is required.
