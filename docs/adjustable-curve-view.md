# AdjustableCurveView

`ru.aleksandr.dccppthrottle.view.AdjustableCurveView` is a standalone integer curve
editor. Its height follows its measured width; use `match_parent` width and
`wrap_content` height. Parent height constraints take precedence when necessary.
Set `app:fitToContainerHeight="true"` (Kotlin: `fitToContainerHeight = true`) to
fill the available container height while retaining full width. This also uses
the height hint supplied by a ScrollView, minus enclosing padding/margins.
Without an available height hint the control falls back to a square. The option
defaults to `false` and is layout configuration, not saved curve data, allowing
portrait and landscape layouts to choose different sizing modes.
The control respects padding and reserves room for markers. Y-axis labels sit
inside the plotting area, to the right of the axis, without an external gutter.
The minimum is at the lower left, X grows rightwards, and Y grows upwards.
Axes are drawn along the bottom and left edges of the plotting area. Y-axis
labels use 10sp text; the active marker's value bubble uses 12sp text.

## Properties

| Kotlin | XML (`app:`) | Default | Behavior |
| --- | --- | --- | --- |
| `minValue` | `min` | `0` | Minimum Y value |
| `maxValue` | `max` | `255` | Maximum Y value; must exceed minimum |
| `gridStep` | `gridStep` | `127` | Positive horizontal grid spacing, starting at the minimum |
| `points` | `points` | Empty list | Integer values; XML references an `integer-array` |
| `smoothing` | `smoothing` | `0` | Neighbor radius, clamped to `0..floor(N/2)`; zero when `N <= 3` |
| `fitToContainerHeight` | `fitToContainerHeight` | `false` | Use available container height instead of square sizing |

`setValueRange(minValue, maxValue)` changes both bounds atomically. Assigning
points or changing bounds clamps values to that range. Point lists are copied
on assignment and retrieval. Assign points before assigning smoothing.

```xml
<ru.aleksandr.dccppthrottle.view.AdjustableCurveView
    android:id="@+id/adjustableCurve"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:min="0"
    app:max="255"
    app:gridStep="127"
    app:points="@array/speed_curve_points"
    app:smoothing="2" />
```

The example requires an `integer-array` resource named `speed_curve_points`.

```kotlin
adjustableCurve.points = listOf(0, 40, 80, 120, 160, 200, 255)
adjustableCurve.smoothing = 2
adjustableCurve.setOnChangeListener { points ->
    // Receives the entire updated curve, including affected neighbors.
}
```

Callbacks report user changes only, not property assignments or state restoration.
`setOnDragStateChangeListener { dragging -> ... }` reports marker capture/release,
including cancellation and detachment, so hosts can lock a ScrollView or pager.
When binding model updates back to the graph, assign `points` only if values differ:
assigning them during a drag ends that drag.
The control saves bounds, grid spacing, points and smoothing across recreation
when it has a stable Android view ID. It uses the current theme's surface text,
primary and on-primary colors. This control does not read or write decoder CVs.

## Editing and interpolation

Markers have equally spaced, fixed X coordinates. Empty lists draw axes and grid
only; a single point is placed at the left edge. Dragging a marker changes its Y
value, with a larger touch target than its visible circle. The initial finger
offset is preserved. A value bubble is shown above the active point, or below
when there is insufficient space at the top. A drag suppresses parent scrolling;
releasing or cancelling it keeps the last edited values and releases scrolling.

The curve uses monotone cubic Hermite interpolation: endpoint tangents equal the
adjacent secant; interior tangents are the harmonic mean of neighboring secants
when their signs agree, otherwise zero. Cubic Bezier segments interpolate every
marker with continuous slopes and no overshoot between adjacent values.

## Neighbor smoothing

Let `b` be the snapshot at drag start, `i` the selected index, `v` its new clamped
integer value, and `S` the effective smoothing radius. Every move is calculated
from `b`, avoiding cumulative rounding and dependence on touch event frequency.

For a neighbor `j` at distance `d = abs(j - i)` within `1..S`:

```text
span = min(S + 1, i if j < i else N - 1 - i)
t = d / span
w = 1 - 10*t^3 + 15*t^4 - 6*t^5
movement = v - b[i]
change = round(movement * w)
result[j] = clamp(b[j] + change, minValue, maxValue)
```

The selected marker receives `v` exactly. Neighbors follow its displacement in
proportion to a quintic falloff. Its first and second derivatives are zero at
both the selected point and the zero-influence boundary, giving a rounded crest
and a gentle transition back to the original curve. The snapshot's existing
shape is carried along with the deformation. Near an endpoint the window is
shortened on that side so the displacement tapers to the fixed endpoint.
Integer rounding and range clipping can still produce small steps or plateaus.

Rounding is to the nearest integer, with half steps away from zero. Returning the
selected marker to its starting value restores the whole snapshot. Points outside
the radius and both endpoints stay unchanged. Endpoints can still be dragged
directly and influence interior neighbors using the same formula.

With `S = 8`, moving a point on a flat curve from 100 to 180 produces neighboring
values of 179, 174, 163, 148, 132, 117, 106 and 101 on each side. The next point
remains at 100. No monotonic-speed restriction is imposed: intentional peaks and
valleys remain editable.

## Verification

```sh
./gradlew :app:testDebugUnitTest :app:packageDebug :app:packageDebugAndroidTest
adb install -r app/build/outputs/apk/debug/dccppthrottle-<version>.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w \
  -e class ru.aleksandr.dccppthrottle.AdjustableCurveViewTest \
  -e curveScreenshots true \
  ru.aleksandr.dccppthrottle.test/androidx.test.runner.AndroidJUnitRunner
adb pull /sdcard/Android/data/ru.aleksandr.dccppthrottle/files/adjustable-curve-review
```

Use a JDK compatible with this project's Gradle version. Eleven JVM tests cover
smooth deformation of flat and straight curves, radius limits, fixed endpoints,
rounding, clipping and interpolation bounds.
Four Android tests cover both light and dark themes, width-driven measurement,
optional container-height sizing inside a padded ScrollView,
clamping, state restoration, callbacks, dragging, cancellation, disabled input,
empty/single-point data and rendering. The optional screenshot flag exports PNGs
for visual review; it does not enable pixel comparisons.
