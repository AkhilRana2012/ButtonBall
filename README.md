# ButtonBall

ButtonBall is a small Android 11-compatible floating control for devices with broken physical Power and Volume buttons. It uses the Android accessibility service only to invoke the system Lock Screen and Power Dialog actions. It never reads window contents, sends network traffic, or uses a wake lock.

## Open and run

1. Start Android Studio and choose **Open**.
2. Select this `buttonball` folder (the folder containing `settings.gradle.kts`).
3. Let Gradle sync, connect the Redmi 9i with USB debugging enabled, and select it as the deployment target.
4. Click **Run**.
5. In the app, choose **Open overlay settings** and enable **Allow display over other apps** for ButtonBall.
6. Choose **Open Accessibility settings**, open **ButtonBall controls**, and turn it on. Android will show its standard accessibility disclosure.
7. Return to ButtonBall. The ball appears when the accessibility service connects. The Start button can show it again; Stop removes it until the service reconnects or Start is pressed.

## Build and install

From this directory, run:

```powershell
.\gradlew.bat assembleDebug
```

The APK is created at `app\build\outputs\apk\debug\app-debug.apk`.

Install it with:

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

On macOS/Linux, use `./gradlew assembleDebug` instead.

## Use the floating control

Drag the blue ball to move it. Its position is saved when the drag ends. Tap it to open the compact menu:

- **Lock** locks the screen immediately.
- **Volume** opens Android’s normal media-volume panel.
- **Power** opens Android’s system power dialog.

Tap anywhere outside the menu to dismiss it. Each action also dismisses it.

## Redmi 9i / MIUI notes

MIUI can restrict background components aggressively. If the control disappears after some time, open the app’s MIUI battery settings and set Battery saver to **No restrictions**. This app cannot bypass MIUI battery management, lock the screen, or show the power dialog until its Accessibility Service is enabled. It deliberately does not request root, device-admin, notification, network, or battery-optimization permissions.
