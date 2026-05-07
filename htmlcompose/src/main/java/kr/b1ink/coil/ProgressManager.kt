package kr.b1ink.coil

import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

const val HEADER_X_PROGRESS_ID = "X-Progress-ID"

internal object ProgressManager {
    private val listeners = ConcurrentHashMap<String, (progress: Float) -> Unit>()

    fun register(id: String, listener: (progress: Float) -> Unit) {
        if (listeners.containsKey(id)) {
            Timber.w("Progress listener already registered for id: $id, replacing")
        }
        listeners[id] = listener
    }

    fun unregister(id: String) {
        listeners.remove(id)
    }

    internal fun onProgress(id: String, progress: Float) {
        listeners[id]?.invoke(progress)
    }
}