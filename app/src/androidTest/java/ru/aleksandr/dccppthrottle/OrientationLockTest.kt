package ru.aleksandr.dccppthrottle

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import ru.aleksandr.dccppthrottle.dialogs.ProgressDialog

@RunWith(AndroidJUnit4::class)
class OrientationLockTest {
    private fun withActivity(test: (ProgActivity) -> Unit) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val activity = instrumentation.startActivitySync(
            Intent(instrumentation.targetContext, ProgActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ) as ProgActivity
        try {
            instrumentation.runOnMainSync { test(activity) }
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
        }
    }

    @Test
    fun overlappingOperationsRestoreOrientationOnlyAfterLastRelease() = withActivity { activity ->
        val original = activity.requestedOrientation
        val first = OrientationLock.acquire(activity)
        val second = OrientationLock.acquire(activity)
        try {
            assertEquals(ActivityInfo.SCREEN_ORIENTATION_LOCKED, activity.requestedOrientation)
            first.release()
            first.release()
            assertEquals(ActivityInfo.SCREEN_ORIENTATION_LOCKED, activity.requestedOrientation)
            second.release()
            assertEquals(original, activity.requestedOrientation)
        } finally {
            first.release()
            second.release()
        }
    }

    @Test
    fun progressDialogReleasesLockOnDismissAndCancel() = withActivity { activity ->
        val original = activity.requestedOrientation
        val dialog = ProgressDialog(activity)
        try {
            dialog.show()
            assertEquals(ActivityInfo.SCREEN_ORIENTATION_LOCKED, activity.requestedOrientation)
            dialog.dismiss()
            assertEquals(original, activity.requestedOrientation)
            dialog.show()
            assertEquals(ActivityInfo.SCREEN_ORIENTATION_LOCKED, activity.requestedOrientation)
            dialog.cancel()
            assertEquals(original, activity.requestedOrientation)
        } finally {
            dialog.dismiss()
        }
    }

    @Test
    fun closingDialogDoesNotUnlockPendingRequest() = withActivity { activity ->
        val original = activity.requestedOrientation
        val dialog = ProgressDialog(activity)
        val request = OrientationLock.acquire(activity)
        try {
            dialog.show()
            dialog.dismiss()
            assertEquals(ActivityInfo.SCREEN_ORIENTATION_LOCKED, activity.requestedOrientation)
            request.release()
            assertEquals(original, activity.requestedOrientation)
        } finally {
            dialog.dismiss()
            request.release()
        }
    }
}
