package ru.aleksandr.dccppthrottle

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.annotation.MainThread
import java.util.WeakHashMap

/** Each operation owns a token; rotation resumes after the last token is released. */
class OrientationLock private constructor(private var activity: Activity?) {
    @MainThread
    fun release() {
        val owner = activity ?: return
        activity = null
        val state = locks[owner] ?: return
        state.count--
        if (state.count == 0) {
            locks.remove(owner)
            if (!owner.isDestroyed) owner.requestedOrientation = state.originalOrientation
        }
    }

    companion object {
        private class State(val originalOrientation: Int, var count: Int = 1)
        private val locks = WeakHashMap<Activity, State>()

        @MainThread
        fun acquire(context: Context): OrientationLock {
            var owner = context
            while (owner is ContextWrapper && owner !is Activity) {
                owner = owner.baseContext
            }
            val activity = owner as? Activity
            if (activity != null) {
                val state = locks[activity]
                if (state != null) {
                    state.count++
                } else {
                    locks[activity] = State(activity.requestedOrientation)
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
                }
            }
            return OrientationLock(activity)
        }
    }
}
