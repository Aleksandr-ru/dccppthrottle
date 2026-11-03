# Programming speed curves

The programming activity has three tabs: **CV** (the original single-CV editor,
selected by default), **Speed Curve**, and **Extended Curve**. The selected tab
and curve edits survive activity recreation. Curve data and mode are kept in an
activity-scoped `SpeedCurveViewModel` with `SavedStateHandle` persistence.
The CV layout, initial UI values and CV29 bit helpers are defined inside that
view model. `MockStore` initializes its simulated decoder independently.

## CV mapping

The mapping follows [NMRA S-9.2.2](https://www.nmra.org/sites/default/files/standards/sandrp/DCC/S/s-9.2.2_decoder_cvs_2012.07.pdf):

| Tab | Graph points in X order | Additional CVs |
| --- | --- | --- |
| Speed Curve | CV2 (start), CV6 (mid), CV5 (maximum) | CV29 bit 4 cleared for simple mode |
| Extended Curve | CV67–CV94 (28 steps) | CV66 forward trim, CV95 reverse trim, CV29 bit 4 set for custom mode |

The two mode switches represent the same shared bit and are complementary.
They edit local state; only **Write** programs the decoder. Graph points and
numeric fields are integer byte values (`0..255`) and update each other.
The drag bubble shows the point's CV number above its current value: CV2/CV6/CV5
for the simple curve and CV67–CV94 for the extended curve.
Trims are separate from the graph, grouped under **Directional speed trim** after the
28-step fields. Each trim field has a short description of its direction and
scale (`value / 128`; zero disables trim, 128 leaves output unchanged).
Extended Curve has an independent **Smoothing**
switch overlaid at the graph's lower right, inset from the bottom and right edges.
It is a sibling Android control, not part of `AdjustableCurveView`.
Smoothing starts enabled (radius eight); disabling it sets the radius to zero.
Toggling preserves CV values and affects subsequent drags only. The preference
survives recreation and is never written to the decoder. The switch is absent
from Speed Curve and disabled during CV transfers.
The simple graph has no neighbor smoothing. Neighbors follow a quintic falloff
of the dragged point's displacement, creating rounded changes of direction. Graph values display CV bytes
directly; decoder-specific meanings of zero/default settings are not substituted.

## Read and write

Both operations use the existing `DecoderFragment.readCv` / `writeCv` helpers
through `ProgrammingFragment`, sharing the command-station request queue, error
handling, cancellation and orientation locks used by decoder settings screens.

- **Read** opens a cancellable progress dialog and reads CV29 plus every data CV
  of that tab. Only a complete successful read updates the model; errors or
  cancellation preserve the edited values.
- **Write** snapshots every numeric value on that tab and the selected mode. It
  first reads the decoder's current CV29. If this fails, nothing is written.
  It then writes every data CV (including unchanged values and both extended
  trims). Finally it writes CV29, changing only bit 4 from the freshly read byte.
  Other bits such as addressing, direction and power conversion are preserved.
- Controls are disabled during a transfer. Cancelling stops subsequent requests;
  already acknowledged writes remain on the decoder. CV29 is written last, so a
  cancelled data transfer does not switch to a partially written table.

With `BuildConfig.DEBUG` and no command station connected, all three tabs use a
shared in-memory mock decoder in `MockStore`. Reads and writes use the existing
asynchronous mock delays, and a successful mock write is returned on later
reads. Release builds and connected debug builds always use the command station.

## Layout and gestures

Portrait uses one `LockableScrollView` for the entire tab. Landscape splits the
screen equally into two independently scrolling areas: graph on the left and
switch, fields and buttons on the right. The landscape graph enables
`fitToContainerHeight`, so its full Y range fits the left viewport without
vertical scrolling. Portrait retains the square graph. Capturing a graph marker locks its
scroll area and ViewPager2 navigation until release/cancellation. Touches outside
markers allow normal scrolling. Read and Write buttons share a horizontal row.

## Verification

```sh
./gradlew :app:testDebugUnitTest :app:packageDebug :app:packageDebugAndroidTest
adb install -r app/build/outputs/apk/debug/dccppthrottle-<version>.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w \
  -e class ru.aleksandr.dccppthrottle.ProgrammingCurvesTest \
  -e programmingScreenshots true \
  ru.aleksandr.dccppthrottle.test/androidx.test.runner.AndroidJUnitRunner
adb pull /sdcard/Android/data/ru.aleksandr.dccppthrottle/files/programming-curves-review /tmp/programming-curves-review
```

Use a JDK supported by the project's Gradle version (JDK 17 in this environment).
The JVM tests verify point order, CV coverage, and preservation of all other CV29
bits for every byte value. Android tests exercise the real UI with disconnected
debug mocks, including bidirectional edits, gesture locks, recreation, complete
read/write round trips, cancellation, the original CV tab and both layouts.
The optional screenshot test captures all three tabs in portrait and landscape
in both themes. Review PNGs in `/tmp/programming-curves-review`.
In filenames, `theme-1` is light, `theme-2` is dark, `land-true` is landscape,
and tabs `0`, `1`, `2` are CV, Speed Curve, Extended Curve respectively.
Physical decoder/command-station operation requires a hardware check.
