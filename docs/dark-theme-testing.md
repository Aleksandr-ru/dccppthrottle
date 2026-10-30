# Dark theme verification

Settings → General → Theme offers System default, Light and Dark. The default
follows the device setting; an explicit selection persists across launches.

## Automated checks

Run with a JDK supported by the existing Gradle/Android plugin configuration:

```sh
./gradlew testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug
```

On an attached Android device or emulator:

```sh
./gradlew connectedDebugAndroidTest
```

`ThemeRecreationTest` checks light/dark settings after activity recreation,
returning to system mode, decoder section titles after recreation, console message
contrast (at least 4.5:1), and independent restoration of numeric inputs (including
an empty nullable input) in both themes. It also checks disabling numeric controls.
The tests restore the original theme preference when they finish.

The optional screenshot review uses in-memory fixtures and fills decoder models
without communicating with hardware. To capture all 62 screen/theme combinations:

```sh
adb shell am instrument -w -e screenshots true \
  ru.aleksandr.dccppthrottle.test/androidx.test.runner.AndroidJUnitRunner
adb pull /sdcard/Android/data/ru.aleksandr.dccppthrottle/files/theme-review
```

Add `-e screen console` to capture only screens whose names contain `console`.
Use `-e screen cab -e speed 50` (or `-e screen dual`) to inspect the speed track
and its partial green fill at a chosen speed from 0 to 100.
Install the application and test APKs first. Temporarily disable emulator window,
transition and animator animations to avoid capturing intermediate frames, then
restore their original settings. Screenshots require visual review; the test does
not compare pixels against golden images.

## Device checklist

Run these checks in both themes and both orientations. Repeat theme switching
with the app open, and relaunch after selecting an explicit theme. In System
default mode, change the system theme while the app is open.

- Connection screen: device list, checkboxes, buttons and startup appearance.
- Main screen: toolbar title/icons, overflow menu, navigation drawer/header,
  locomotive/accessory/route lists and their context menus.
- Single and dual cabs: selected locomotives/pages, direction icons, speed labels,
  slider track/thumb/ticks, function buttons and track-power snackbar.
- Locomotive/accessory/route editors and POM dialogs: text, inputs, selected states,
  disabled controls and restored input values.
- Programming screen and each decoder helper: CV number/value/bit controls,
  dropdowns, chips, tables, read/write actions and validation messages.
- Console and settings: readable text, status colors and preference dialogs.
- System status/navigation bars and transitions: readable icons and backgrounds.

With a command station connected, switching themes must preserve the connection,
selected locomotive, speed, direction, function states and track power. Check that
recreating the programming screen does not send a track-join command. Returning
explicitly to the connection screen must retain the normal disconnect behavior.

## Verification in the development environment

The 42 JVM tests pass. Application and instrumentation-test APKs compile.
On the Android 16 (API 36) x86_64 emulator, all six instrumentation tests pass
(including the optional screenshot review). The full review captured 62 PNGs:
31 screens/configurations in each theme. Console screenshots and contrast checks
were repeated after the console palette fix. Single/dual cabs and programming were
also checked in landscape. Decoder review covered the initially visible contents
of all 13 helper sections using loaded test models.

Changing the system night mode while the connection screen was open switched its
background between #121212 and #FAFAFA without relaunching the app. The original
system night mode and animation settings were restored afterward.

This emulator review found and fixed lost decoder section titles after recreation
and low-contrast console messages. Real Bluetooth/command-station behavior remains
unverified and requires hardware.

Full lint reports the same 371 errors and 201 warnings as the original revision,
with no new findings. Existing translation and other unrelated lint errors remain
outside this change.
